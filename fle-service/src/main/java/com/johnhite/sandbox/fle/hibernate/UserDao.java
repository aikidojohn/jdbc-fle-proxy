package com.johnhite.sandbox.fle.hibernate;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserDao extends AbstractDAO<UserEntity> {
    @Inject
    public UserDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }


    public UserEntity findById(long id) {
        return get(id);
    }

    public UserEntity persist(UserEntity entity) {
        return super.persist(entity);
    }
}
