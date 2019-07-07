package com.johnhite.sandbox.fle.db.proxy;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class FLEDriverProxy implements Driver {

    //
    // Register ourselves with the DriverManager
    //
    static {
        try {
            DriverManager.registerDriver(new FLEDriverProxy());
        } catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }

    private Driver proxiedDriver = null;

    private void initializeDriver(Properties info) throws SQLException{
        String driver = info.getProperty("driver");
        if (driver == null) {
            throw new SQLException("Driver implementation not specified in the 'driver' property");
        }
        try {
            proxiedDriver = (Driver)Class.forName(driver).newInstance();
            DriverManager.deregisterDriver(proxiedDriver);
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                System.out.println(drivers.nextElement().getClass());
            }
        } catch(ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new SQLException("Unable to load driver implementation class", e);
        }
    }

    @Override public Connection connect(String url, Properties info) throws SQLException {
        if (proxiedDriver == null) {
            initializeDriver(info);
        }

        return FLEConnectionProxy.wrap(proxiedDriver.connect(url, info));
    }

    @Override public boolean acceptsURL(String url) throws SQLException {
        if (proxiedDriver == null) {
            throw new SQLException("Driver not initialized");
        }
        return proxiedDriver.acceptsURL(url);
    }

    @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        if (proxiedDriver == null) {
            initializeDriver(info);
        }
        return proxiedDriver.getPropertyInfo(url, info);
    }

    @Override public int getMajorVersion() {
        return proxiedDriver == null ? 0 : proxiedDriver.getMajorVersion();
    }

    @Override public int getMinorVersion() {
        return proxiedDriver == null ? 0 : proxiedDriver.getMinorVersion();
    }

    @Override public boolean jdbcCompliant() {
        return proxiedDriver == null ? false : proxiedDriver.jdbcCompliant();
    }

    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        if (proxiedDriver == null) {
            throw new SQLFeatureNotSupportedException();
        }
        return proxiedDriver.getParentLogger();
    }
}
