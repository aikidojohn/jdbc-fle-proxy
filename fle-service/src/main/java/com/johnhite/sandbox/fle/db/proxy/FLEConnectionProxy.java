package com.johnhite.sandbox.fle.db.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class FLEConnectionProxy implements InvocationHandler {

    private Connection connection;
    public FLEConnectionProxy(Connection connection) {
        this.connection = connection;
    }

    public static Connection wrap(Connection c) {
        return (Connection) Proxy.newProxyInstance(FLEConnectionProxy.class.getClassLoader(), new Class[] {Connection.class}, new FLEConnectionProxy(c));
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("createStatement")) {
            Statement s = (Statement)method.invoke(connection, args);
            return FLEStatementProxy.wrap(s);
        }
        else if (method.getName().equals("prepareStatement")) {
            PreparedStatement p = (PreparedStatement)method.invoke(connection, args);
            return FLEStatementProxy.wrap(p, (String)args[0]);
        }
        return method.invoke(connection, args);
    }
}
