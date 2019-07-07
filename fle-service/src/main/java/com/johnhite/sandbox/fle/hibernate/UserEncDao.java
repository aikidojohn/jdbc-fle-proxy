package com.johnhite.sandbox.fle.hibernate;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserEncDao extends AbstractDAO<UserEncEntity> {
    @Inject
    public UserEncDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    public UserEncEntity findById(long id) {
        return get(id);
    }

    public UserEncEntity persist(UserEncEntity entity) {
        return super.persist(entity);
    }
}
