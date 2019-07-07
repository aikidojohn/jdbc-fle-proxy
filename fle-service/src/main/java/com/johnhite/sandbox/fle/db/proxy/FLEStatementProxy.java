package com.johnhite.sandbox.fle.db.proxy;

import com.johnhite.sandbox.fle.crypto.BlockEncryption;
import com.johnhite.sandbox.fle.crypto.CipherParameters;
import com.johnhite.sandbox.fle.crypto.EncryptionManager;
import com.johnhite.sandbox.fle.crypto.KeyManager;

import javax.crypto.SecretKey;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FLEStatementProxy {

    public static Statement wrap(Statement s) {
        return (Statement) Proxy.newProxyInstance(FLEStatementProxy.class.getClassLoader(), new Class[] {Statement.class}, new FLEStatementProxyImpl(s));
    }

    public static PreparedStatement wrap(PreparedStatement s, String query) {
        return (PreparedStatement) Proxy.newProxyInstance(FLEStatementProxy.class.getClassLoader(), new Class[] {PreparedStatement.class}, new FLEPreparedStatementProxyImpl(s, query));
    }

    public static ResultSet wrap(ResultSet r) {
        return (ResultSet) Proxy.newProxyInstance(FLEStatementProxy.class.getClassLoader(), new Class[] {ResultSet.class}, new FLEResultSetProxyImpl(r));
    }

    public static class FLEStatementProxyImpl implements InvocationHandler {
        private Statement statement;

        public FLEStatementProxyImpl(Statement statement) {
            this.statement = statement;
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("Statement." + method.getName());
            return method.invoke(statement, args);
        }
    }

    public static class FLEPreparedStatementProxyImpl implements InvocationHandler {

        private PreparedStatement statement;
        private String query;
        private Map<Integer,String> params;
        private String table;

        public FLEPreparedStatementProxyImpl(PreparedStatement statement, String query) {
            this.statement = statement;
            this.query = query;
        }
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("PreparedStatement." + method.getName());
            ParameterMetaData md = statement.getParameterMetaData();

            if (method.getName().equals("getResultSet")) {
                ResultSet rs = (ResultSet)method.invoke(statement, args);
                return FLEStatementProxy.wrap(rs);
            }
            else if (method.getName().equals("executeUpdate")) {
                return statement.executeUpdate();
            }
            else if (method.getName().equals("executeQuery")) {
                ResultSet rs = (ResultSet)method.invoke(statement, args);
                return FLEStatementProxy.wrap(rs);
            }
            else if (method.getName().equals("setBytes")) {
                if (params == null) {
                    params = extractParameterNames();
                }
                int columnNumber = (int)args[0];
                String column = params.get(columnNumber);
                FieldConf fieldConf = EncryptionConf.getInstance().getField(table + "." + column);
                if (fieldConf != null) {
                    SecretKey key = KeyManager.getInstance().getCurrentKey();
                    byte[] encrypted = EncryptionManager.getEncryption(fieldConf.getFormat(), byte[].class)
                            .encrypt(key, column.getBytes(), (byte[])args[1]);
                    return method.invoke(statement, new Object[]{args[0], encrypted});
                }
            }
            else if (method.getName().equals("setString")) {
                if (params == null) {
                    params = extractParameterNames();
                }
                int columnNumber = (int)args[0];
                String column = params.get(columnNumber);
                FieldConf fieldConf = EncryptionConf.getInstance().getField(table + "." + column);
                if (fieldConf != null) {
                    SecretKey key = KeyManager.getInstance().getCurrentKey();
                    String encrypted = EncryptionManager.getEncryption(fieldConf.getFormat(), String.class)
                            .encrypt(key, column.getBytes(), (String)args[1]);
                    return method.invoke(statement, new Object[]{columnNumber, encrypted});
                }
            }
            return method.invoke(statement, args);
        }

        private Map<Integer, String> extractParameterNames() {
            Map<Integer, String> params = new HashMap<>();
            if (query == null) {
                return params;
            }
            String querylc = query.toLowerCase();
            int insertIdx = querylc.indexOf("insert into");
            if (insertIdx < 0) {
                return params;
            }

            String[] p = query.substring(insertIdx).split(" ");
            table = p[2];
            int parenIdx = query.indexOf('(', insertIdx);
            int thesisIdx = query.indexOf(')', parenIdx);
            String[] parts = query.substring(parenIdx+1, thesisIdx).split(",");
            for (int i=0; i < parts.length; i++) {
                params.put(i+1, parts[i].trim());
            }
            return params;
        }
    }

    public static class FLEResultSetProxyImpl implements InvocationHandler {

        private ResultSet rs;
        private Map<String, String> labelTable = new HashMap<>();
        private Map<String, Integer> indexTable = new HashMap<>();
        private ResultSetMetaData md;

        public FLEResultSetProxyImpl(ResultSet rs) {
            this.rs = rs;
            try {
                md = rs.getMetaData();
                int count = md.getColumnCount();
                for (int i = 1; i < count; i++) {
                    labelTable.put(md.getColumnLabel(i), md.getColumnName(i));
                    indexTable.put(md.getColumnLabel(i), i);
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
        }

        public String getTableName(Object labelOrIndex) throws Throwable {
            if (labelOrIndex instanceof String) {
                Integer index = indexTable.get(labelOrIndex);
                if (index == null) {
                    return null;
                }
                return md.getTableName(index);
            } else {
                return md.getTableName((int)labelOrIndex);
            }
        }
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("ResultSet." + method.getName() + "\t" + Arrays.toString(args));
            if (args == null || args.length == 0) {
                return method.invoke(rs, args);
            }
            String table = getTableName(args[0]);
            if (table == null ) {
                return method.invoke(rs, args);
            }
            String column = labelTable.get(args[0]); //TODO Fix for integer argument
            FieldConf fieldConf = EncryptionConf.getInstance().getField(table + "." + column);
            if (fieldConf == null) {
                return method.invoke(rs, args);
            }

            if (method.getName().equals("getBytes")) {
                System.out.println("decrypting " + column);
                byte[] encrypted = (byte[])method.invoke(rs, args);

                SecretKey key = KeyManager.getInstance().getCurrentKey();
                byte[] decrypted = EncryptionManager.getEncryption(fieldConf.getFormat(), byte[].class)
                        .decrypt(key, column.getBytes(), encrypted);
                return decrypted;
            }
            else if (method.getName().equals("getString")) {
                System.out.println("decrypting " + column);
                String encrypted = (String)method.invoke(rs, args);

                SecretKey key = KeyManager.getInstance().getCurrentKey();
                String decrypted = EncryptionManager.getEncryption(fieldConf.getFormat(), String.class)
                        .decrypt(key, column.getBytes(), encrypted);
                return decrypted;
            }
            else if (method.getName().equals("getBigInteger")) {
                System.out.println("decrypting " + column);
                BigInteger encrypted = (BigInteger)method.invoke(rs, args);

                SecretKey key = KeyManager.getInstance().getCurrentKey();
                BigInteger decrypted = EncryptionManager.getEncryption(fieldConf.getFormat(), BigInteger.class)
                        .decrypt(key, column.getBytes(), encrypted);
                return decrypted;
            }
            return method.invoke(rs, args);
        }
    }
}
