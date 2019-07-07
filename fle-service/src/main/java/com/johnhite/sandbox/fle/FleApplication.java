package com.johnhite.sandbox.fle;



import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.johnhite.sandbox.fle.crypto.KeyManager;
import com.johnhite.sandbox.fle.db.proxy.EncryptionConf;
import com.johnhite.sandbox.fle.db.proxy.FieldConf;
import com.johnhite.sandbox.fle.hibernate.UserEncEntity;
import com.johnhite.sandbox.fle.hibernate.UserEntity;
import com.johnhite.sandbox.fle.hibernate.KeyEntity;
import com.johnhite.sandbox.fle.hibernate.UserKeyEntity;
import com.johnhite.sandbox.fle.resources.UserResource;
import com.rtr.wizard.RequiredBundle;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class FleApplication extends Application<FleConfiguration> {

    private static Class<?>[] RESOURCES = {
            UserResource.class
    };
    private final HibernateBundle<FleConfiguration> hibernate = new HibernateBundle<FleConfiguration>(UserEntity.class, UserEncEntity.class, KeyEntity.class, UserKeyEntity.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(FleConfiguration configuration) {
            return configuration.getDatabase();
        }
    };

    @Override
    public String getName() {
        return "fle-service";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void initialize(final Bootstrap<FleConfiguration> bootstrap) {
        final RequiredBundle<FleConfiguration> requiredBundle = new RequiredBundle<>();
        bootstrap.addBundle(requiredBundle);
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(final FleConfiguration configuration,
                    final Environment environment) throws Exception {

        environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final Injector injector = Guice.createInjector(new FleModule(environment,configuration, hibernate.getSessionFactory()));

        final KeyManager km = injector.getInstance(KeyManager.class);
        KeyManager.setInstance(km);

        EncryptionConf.getInstance().addField(new FieldConf("users.mail", FieldConf.FieldFormat.EMAIL));
        EncryptionConf.getInstance().addField(new FieldConf("users.first_name", FieldConf.FieldFormat.ENGLISH_NAME));
        EncryptionConf.getInstance().addField(new FieldConf("users.last_name", FieldConf.FieldFormat.ENGLISH_NAME));
        EncryptionConf.getInstance().addField(new FieldConf("users.address1", FieldConf.FieldFormat.ENGLISH_TEXT));
        EncryptionConf.getInstance().addField(new FieldConf("users.address2", FieldConf.FieldFormat.ENGLISH_TEXT));
        EncryptionConf.getInstance().addField(new FieldConf("users.phone_number", FieldConf.FieldFormat.NUMBER_FIXED_WIDTH));

        for (Class<?> resource : RESOURCES) {
            environment.jersey().register(injector.getInstance(resource));
        }
    }

    public static void main(String... args) throws Exception {
        new FleApplication().run(args);
    }
}
