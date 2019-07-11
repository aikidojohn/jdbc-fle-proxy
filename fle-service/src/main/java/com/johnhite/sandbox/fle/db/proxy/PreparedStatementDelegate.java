package com.johnhite.sandbox.fle.db.proxy;

import com.johnhite.sandbox.fle.crypto.Encryption;
import com.johnhite.sandbox.fle.crypto.EncryptionManager;
import com.johnhite.sandbox.fle.crypto.KeyManager;

import javax.crypto.SecretKey;
import javax.swing.text.Keymap;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PreparedStatementDelegate extends StatementDelegate implements PreparedStatement {
    private final PreparedStatement wrapped;
    private List<EncryptionCommand> encryptionCommands = new LinkedList<>();
    private Map<Integer, String> params = new HashMap<>();
    private String query;
    private String tableName;
    private SecretKey currentKey;
    private long currentKeyId = -1;
    private Set<String> idColumnsSet = new HashSet<>();

    public PreparedStatementDelegate(PreparedStatement wrapped, String query) {
        super(wrapped);
        this.wrapped = wrapped;
        this.query = query;
        extractParameterNames();
    }

    private void extractParameterNames() {
        if (query == null) {
            return;
        }
        String querylc = query.toLowerCase();
        int insertIdx = querylc.indexOf("insert into");
        if (insertIdx < 0) {
            return;
        }

        String[] p = query.substring(insertIdx).split(" ");
        tableName = p[2];
        int parenIdx = query.indexOf('(', insertIdx);
        int thesisIdx = query.indexOf(')', parenIdx);
        String[] parts = query.substring(parenIdx+1, thesisIdx).split(",");
        for (int i=0; i < parts.length; i++) {
            params.put(i+1, parts[i].trim());
        }
    }

    @Override public ResultSet executeQuery() throws SQLException {
        ResultSet rs = wrapped.executeQuery();
        return new ResultSetDelegate(rs);
    }

    @Override public int executeUpdate() throws SQLException {
        if (currentKey == null) {
            Optional<TableConf> conf =  EncryptionConf.getInstance().getTable(tableName);
            if (conf.isPresent()) {
                currentKey = KeyManager.getInstance().generateTransientKey();
                currentKeyId = KeyManager.getInstance().persistKey(currentKey);
                String column = null;
                for (String c : conf.get().getKeyIdAttrs().keySet()) {
                    if (!idColumnsSet.contains(c)) {
                        column = c;
                        break;
                    }
                }
            }
        }

        for (EncryptionCommand com : encryptionCommands) {
            try {
                com.encrypt(currentKey);
            } catch (SQLException e) {
                throw e;
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }
        int updated = wrapped.executeUpdate();

        //persist generated id if needed
        Optional<TableConf> conf =  EncryptionConf.getInstance().getTable(tableName);
        if (conf.isPresent()) {
            if (idColumnsSet.size() != conf.get().getKeyIdAttrs().size()) {
                ResultSet rs = wrapped.getGeneratedKeys();
                if (rs.next()) {
                    long id = rs.getLong(1);
                    String col = null;
                    for (String c : conf.get().getKeyIdAttrs().keySet()) {
                        if (!idColumnsSet.contains(c)) {
                            col = c;
                            break;
                        }
                    }
                    KeyManager.getInstance().persistKeyMapping(currentKeyId, id, conf.get().getKeyIdAttrs().get(col));
                } else {
                    System.out.println("couldn't find generated id");
                }
            }
        }
        return updated;
    }

    @Override public void setNull(int parameterIndex, int sqlType) throws SQLException {
        wrapped.setNull(parameterIndex, sqlType);
    }

    @Override public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        wrapped.setBoolean(parameterIndex, x);
    }

    @Override public void setByte(int parameterIndex, byte x) throws SQLException {
        wrapped.setByte(parameterIndex, x);
    }

    @Override public void setShort(int parameterIndex, short x) throws SQLException {
        wrapped.setShort(parameterIndex, x);
    }

    @Override public void setInt(int parameterIndex, int x) throws SQLException {
        wrapped.setInt(parameterIndex, x);
    }

    @Override public void setLong(int parameterIndex, long x) throws SQLException {
        wrapped.setLong(parameterIndex, x);
    }

    @Override public void setFloat(int parameterIndex, float x) throws SQLException {
        wrapped.setFloat(parameterIndex, x);
    }

    @Override public void setDouble(int parameterIndex, double x) throws SQLException {
        wrapped.setDouble(parameterIndex, x);
    }

    @Override public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        wrapped.setBigDecimal(parameterIndex, x);
    }

    private void lookupKeyForField(int parameterIndex) {
        final String column = params.get(parameterIndex);

        //Is column a key identifier?
        Optional<TableConf> t = EncryptionConf.getInstance().getTable(tableName);
        if (t.isPresent()) {
            final String idName = t.get().getKeyIdAttrs().get(column);
            if (idName != null) {
                //This is an ID Field - try to find a key.
                Optional<SecretKey> key = KeyManager.getInstance().getKeyByUserAttribute(column, idName);
                if (currentKey == null && key.isPresent()) {
                    currentKey = key.get();
                } else if (currentKey == null) {
                    //generate key;
                    currentKey = KeyManager.getInstance().generateTransientKey();
                    currentKeyId = KeyManager.getInstance().persistKey(currentKey);
                    idColumnsSet.add(column);
                    EncryptionCommand command = (sk) -> {
                        KeyManager.getInstance().persistKeyMapping(currentKeyId, column, idName);
                    };
                    encryptionCommands.add(command);
                } else if (currentKeyId > 0) {
                    idColumnsSet.add(column);
                    EncryptionCommand command = (sk) -> {
                        KeyManager.getInstance().persistKeyMapping(currentKeyId, column, idName);
                    };
                    encryptionCommands.add(command);
                }
            }
        }
    }

    @Override public void setString(int parameterIndex, final String x) throws SQLException {
        if (params.isEmpty()) {
            wrapped.setString(parameterIndex, x);
            return;
        }

        //Is column a key identifier?
        lookupKeyForField(parameterIndex);

        //Should Field be encrypted?
        final String column = params.get(parameterIndex);
        final Optional<FieldConf> fieldConf = EncryptionConf.getInstance().getField(tableName, column);
        if (fieldConf.isPresent()) {
            //wrap in a command to encrypt later.
            EncryptionCommand command = (sk) -> {
                String encrypted = EncryptionManager.getEncryption(fieldConf.get().getFormat(), String.class)
                        .encrypt(sk, new byte[]{1}, x);
                wrapped.setString(parameterIndex, encrypted);
            };
            encryptionCommands.add(command);
            return;
        }
        wrapped.setString(parameterIndex, x);
    }

    @Override public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        wrapped.setBytes(parameterIndex, x);
    }

    @Override public void setDate(int parameterIndex, Date x) throws SQLException {
        wrapped.setDate(parameterIndex, x);
    }

    @Override public void setTime(int parameterIndex, Time x) throws SQLException {
        wrapped.setTime(parameterIndex, x);
    }

    @Override public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        wrapped.setTimestamp(parameterIndex, x);
    }

    @Override public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrapped.setAsciiStream(parameterIndex, x, length);
    }

    @Override public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrapped.setUnicodeStream(parameterIndex, x, length);
    }

    @Override public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        wrapped.setBinaryStream(parameterIndex, x, length);
    }

    @Override public void clearParameters() throws SQLException {
        wrapped.clearParameters();
    }

    @Override public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        wrapped.setObject(parameterIndex, x, targetSqlType);
    }

    @Override public void setObject(int parameterIndex, Object x) throws SQLException {
        wrapped.setObject(parameterIndex, x);
    }

    @Override public boolean execute() throws SQLException {
        return wrapped.execute();
    }

    @Override public void addBatch() throws SQLException {
        wrapped.addBatch();
    }

    @Override public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        wrapped.setCharacterStream(parameterIndex, reader, length);
    }

    @Override public void setRef(int parameterIndex, Ref x) throws SQLException {
        wrapped.setRef(parameterIndex, x);
    }

    @Override public void setBlob(int parameterIndex, Blob x) throws SQLException {
        wrapped.setBlob(parameterIndex, x);
    }

    @Override public void setClob(int parameterIndex, Clob x) throws SQLException {
        wrapped.setClob(parameterIndex, x);
    }

    @Override public void setArray(int parameterIndex, Array x) throws SQLException {
        wrapped.setArray(parameterIndex, x);
    }

    @Override public ResultSetMetaData getMetaData() throws SQLException {
        return wrapped.getMetaData();
    }

    @Override public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        wrapped.setDate(parameterIndex, x, cal);
    }

    @Override public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        wrapped.setTime(parameterIndex, x, cal);
    }

    @Override public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        wrapped.setTimestamp(parameterIndex, x, cal);
    }

    @Override public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        wrapped.setNull(parameterIndex, sqlType, typeName);
    }

    @Override public void setURL(int parameterIndex, URL x) throws SQLException {
        wrapped.setURL(parameterIndex, x);
    }

    @Override public ParameterMetaData getParameterMetaData() throws SQLException {
        return wrapped.getParameterMetaData();
    }

    @Override public void setRowId(int parameterIndex, RowId x) throws SQLException {
        wrapped.setRowId(parameterIndex, x);
    }

    @Override public void setNString(int parameterIndex, String value) throws SQLException {
        wrapped.setNString(parameterIndex, value);
    }

    @Override public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        wrapped.setNCharacterStream(parameterIndex, value, length);
    }

    @Override public void setNClob(int parameterIndex, NClob value) throws SQLException {
        wrapped.setNClob(parameterIndex, value);
    }

    @Override public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        wrapped.setClob(parameterIndex, reader, length);
    }

    @Override public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        wrapped.setBlob(parameterIndex, inputStream, length);
    }

    @Override public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        wrapped.setNClob(parameterIndex, reader, length);
    }

    @Override public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        wrapped.setSQLXML(parameterIndex, xmlObject);
    }

    @Override public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        wrapped.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        wrapped.setAsciiStream(parameterIndex, x, length);
    }

    @Override public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        wrapped.setBinaryStream(parameterIndex, x, length);
    }

    @Override public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        wrapped.setCharacterStream(parameterIndex, reader, length);
    }

    @Override public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        wrapped.setAsciiStream(parameterIndex,x);
    }

    @Override public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        wrapped.setBinaryStream(parameterIndex, x);
    }

    @Override public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        wrapped.setCharacterStream(parameterIndex, reader);
    }

    @Override public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        wrapped.setNCharacterStream(parameterIndex,value);
    }

    @Override public void setClob(int parameterIndex, Reader reader) throws SQLException {
        wrapped.setClob(parameterIndex, reader);
    }

    @Override public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        wrapped.setBlob(parameterIndex, inputStream);
    }

    @Override public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        wrapped.setNClob(parameterIndex, reader);
    }

    @Override public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        wrapped.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        wrapped.setObject(parameterIndex, x, targetSqlType);
    }

    @Override public long executeLargeUpdate() throws SQLException {
        return wrapped.executeLargeUpdate();
    }

}
