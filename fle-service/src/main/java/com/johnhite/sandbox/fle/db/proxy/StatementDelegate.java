package com.johnhite.sandbox.fle.db.proxy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class StatementDelegate implements Statement {
    private final Statement wrapped;
    public StatementDelegate(Statement wrapped) {
        this.wrapped = wrapped;
    }
    @Override public ResultSet executeQuery(String sql) throws SQLException {
        return wrapped.executeQuery(sql);
    }

    @Override public int executeUpdate(String sql) throws SQLException {
        return wrapped.executeUpdate(sql);
    }

    @Override public void close() throws SQLException {
        wrapped.close();
    }

    @Override public int getMaxFieldSize() throws SQLException {
        return wrapped.getMaxFieldSize();
    }

    @Override public void setMaxFieldSize(int max) throws SQLException {
        wrapped.setMaxFieldSize(max);
    }

    @Override public int getMaxRows() throws SQLException {
        return wrapped.getMaxRows();
    }

    @Override public void setMaxRows(int max) throws SQLException {
        wrapped.setMaxRows(max);
    }

    @Override public void setEscapeProcessing(boolean enable) throws SQLException {
        wrapped.setEscapeProcessing(true);
    }

    @Override public int getQueryTimeout() throws SQLException {
        return wrapped.getQueryTimeout();
    }

    @Override public void setQueryTimeout(int seconds) throws SQLException {
        wrapped.setQueryTimeout(seconds);
    }

    @Override public void cancel() throws SQLException {
        wrapped.cancel();
    }

    @Override public SQLWarning getWarnings() throws SQLException {
        return wrapped.getWarnings();
    }

    @Override public void clearWarnings() throws SQLException {
        wrapped.clearWarnings();
    }

    @Override public void setCursorName(String name) throws SQLException {
        wrapped.setCursorName(name);
    }

    @Override public boolean execute(String sql) throws SQLException {
        return wrapped.execute(sql);
    }

    @Override public ResultSet getResultSet() throws SQLException {
        return new ResultSetDelegate(wrapped.getResultSet());
    }

    @Override public int getUpdateCount() throws SQLException {
        return wrapped.getUpdateCount();
    }

    @Override public boolean getMoreResults() throws SQLException {
        return wrapped.getMoreResults();
    }

    @Override public void setFetchDirection(int direction) throws SQLException {
        wrapped.setFetchDirection(direction);
    }

    @Override public int getFetchDirection() throws SQLException {
        return wrapped.getFetchDirection();
    }

    @Override public void setFetchSize(int rows) throws SQLException {
        wrapped.setFetchSize(rows);
    }

    @Override public int getFetchSize() throws SQLException {
        return wrapped.getFetchSize();
    }

    @Override public int getResultSetConcurrency() throws SQLException {
        return wrapped.getResultSetConcurrency();
    }

    @Override public int getResultSetType() throws SQLException {
        return wrapped.getResultSetType();
    }

    @Override public void addBatch(String sql) throws SQLException {
        wrapped.addBatch(sql);
    }

    @Override public void clearBatch() throws SQLException {
        wrapped.clearBatch();
    }

    @Override public int[] executeBatch() throws SQLException {
        return wrapped.executeBatch();
    }

    @Override public Connection getConnection() throws SQLException {
        return wrapped.getConnection();
    }

    @Override public boolean getMoreResults(int current) throws SQLException {
        return wrapped.getMoreResults(current);
    }

    @Override public ResultSet getGeneratedKeys() throws SQLException {
        return wrapped.getGeneratedKeys();
    }

    @Override public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return wrapped.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return wrapped.executeUpdate(sql, columnIndexes);
    }

    @Override public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return wrapped.executeUpdate(sql, columnNames);
    }

    @Override public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return wrapped.execute(sql, autoGeneratedKeys);
    }

    @Override public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return wrapped.execute(sql, columnIndexes);
    }

    @Override public boolean execute(String sql, String[] columnNames) throws SQLException {
        return wrapped.execute(sql, columnNames);
    }

    @Override public int getResultSetHoldability() throws SQLException {
        return wrapped.getResultSetHoldability();
    }

    @Override public boolean isClosed() throws SQLException {
        return wrapped.isClosed();
    }

    @Override public void setPoolable(boolean poolable) throws SQLException {
        wrapped.setPoolable(poolable);
    }

    @Override public boolean isPoolable() throws SQLException {
        return wrapped.isPoolable();
    }

    @Override public void closeOnCompletion() throws SQLException {
        wrapped.closeOnCompletion();
    }

    @Override public boolean isCloseOnCompletion() throws SQLException {
        return wrapped.isCloseOnCompletion();
    }

    @Override public <T> T unwrap(Class<T> iface) throws SQLException {
        return wrapped.unwrap(iface);
    }

    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return wrapped.isWrapperFor(iface);
    }
}
