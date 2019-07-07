package com.johnhite.sandbox.fle.hibernate;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class KeyDao extends AbstractDAO<KeyEntity> {
    private SessionFactory sessionFactory;
    @Inject
    public KeyDao(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public KeyEntity findById(long id) {
        return get(id);
    }

    public KeyEntity persist(KeyEntity entity) {
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
