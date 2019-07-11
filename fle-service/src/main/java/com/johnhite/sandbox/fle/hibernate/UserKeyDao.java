package com.johnhite.sandbox.fle.hibernate;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class UserKeyDao extends AbstractDAO<UserKeyEntity> {
    private SessionFactory sessionFactory;
    @Inject
    public UserKeyDao(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public Optional<UserKeyEntity> findById(byte[] id) {
        return Optional.ofNullable(get(id));
    }

    public UserKeyEntity persist(UserKeyEntity entity) {
        /*Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.saveOrUpdate(entity);
            tx.commit();
            return entity;
        } catch ( Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }*/

        return super.persist(entity);
    }
}
