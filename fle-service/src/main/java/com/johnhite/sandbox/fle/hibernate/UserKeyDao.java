package com.johnhite.sandbox.fle.hibernate;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

@Singleton
public class UserKeyDao extends AbstractDAO<UserKeyEntity> {
    private SessionFactory sessionFactory;
    @Inject
    public UserKeyDao(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public long createEmailId(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(email.getBytes());
            long id = new BigInteger(1, Arrays.copyOfRange(hash, 0, 7)).longValue();
            return id;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public UserKeyEntity findByEmail(String email) {
        return get(createEmailId(email));
    }

    public UserKeyEntity findByUserId(long userId) {
        try {
            return (UserKeyEntity)criteria().add(Restrictions.eq("userId", userId)).uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
