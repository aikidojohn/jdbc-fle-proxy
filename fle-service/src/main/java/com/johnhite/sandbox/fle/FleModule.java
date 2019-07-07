package com.johnhite.sandbox.fle;

import com.google.inject.AbstractModule;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;

public class FleModule extends AbstractModule {

    private Environment env;
    private FleConfiguration config;
    private SessionFactory sessionFactory;

    public FleModule(Environment env, FleConfiguration config, SessionFactory sessionFactory) {
        this.env = env;
        this.config = config;
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void configure() {

        bind(SessionFactory.class).toInstance(sessionFactory);
    }
}
