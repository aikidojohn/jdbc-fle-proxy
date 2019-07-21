package com.johnhite.sandbox.fle.db.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class FLEStatementProxy {

    public static Statement wrap(Statement s) {
        return (Statement) Proxy.newProxyInstance(FLEStatementProxy.class.getClassLoader(), new Class[] {Statement.class}, new FLEStatementProxyImpl(s));
    }

    public static PreparedStatement wrap(PreparedStatement s, String query) {
        return new PreparedStatementDelegate(s, query);
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
}
