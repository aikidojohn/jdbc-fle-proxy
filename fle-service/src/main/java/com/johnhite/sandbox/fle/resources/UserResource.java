package com.johnhite.sandbox.fle.resources;

import com.johnhite.sandbox.fle.crypto.BlockEncryption;
import com.johnhite.sandbox.fle.crypto.CipherParameters;
import com.johnhite.sandbox.fle.crypto.KeyManager;
import com.johnhite.sandbox.fle.hibernate.UserDao;
import com.johnhite.sandbox.fle.hibernate.UserEncDao;
import com.johnhite.sandbox.fle.hibernate.UserEncEntity;
import com.johnhite.sandbox.fle.hibernate.UserEntity;
import com.johnhite.sandbox.fle.hibernate.UserKeyDao;
import com.johnhite.sandbox.fle.hibernate.UserKeyEntity;
import io.dropwizard.hibernate.UnitOfWork;
import jdk.nashorn.internal.ir.ReturnNode;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

@Path("users")
@Produces({ MediaType.APPLICATION_JSON})
@Consumes({ MediaType.APPLICATION_JSON})
public class UserResource {
    private UserDao userDao;
    private UserEncDao userEncDao;
    private UserKeyDao userKeyDao;

    @Inject
    public UserResource(UserDao userDao, UserEncDao userEncDao, UserKeyDao userKeyDao) {
        this.userDao = userDao;
        this.userEncDao = userEncDao;
        this.userKeyDao = userKeyDao;
    }

    @Path("{id}")
    @GET
    @UnitOfWork(readOnly = true)
    public Response getById(@PathParam("id") long id) {
        UserKeyEntity uke = userKeyDao.findByUserId(id);
        if (uke != null) {
            KeyManager.getInstance().setCurrentKeyId(uke.getKey());
        }
        return Response.ok(userDao.findById(id)).build();
    }

    @POST
    @UnitOfWork
    public Response createUser( UserEntity user) {
        UserEntity resp = null;
        UserKeyEntity uke = userKeyDao.findByEmail(user.getMail());
        if (uke != null) {
            KeyManager.getInstance().setCurrentKeyId(uke.getKey());
            resp = userDao.persist(user);
        }
        else {
            KeyManager.getInstance().getCurrentKey();
            long keyId = KeyManager.getInstance().getCurrentKeyId();
            resp = userDao.persist(user);
            uke = new UserKeyEntity(resp.getId(), userKeyDao.createEmailId(user.getMail()), keyId);
            userKeyDao.persist(uke);
        }


        return Response.ok(resp).build();
    }

    @Path("enc/{id}")
    @GET
    @UnitOfWork(readOnly = true)
    public Response getEncById(@PathParam("id") long id) {
        return Response.ok(userEncDao.findById(id)).build();
    }

    @Path("enc")
    @POST
    @UnitOfWork
    public Response createUserEnc( UserEntity user) {
        UserEncEntity enc = new UserEncEntity(
                user.getMail().getBytes(),
                user.getFirstName().getBytes(),
                user.getLastName().getBytes(),
                null,
                user.getAddress1().getBytes(),
                user.getAddress2().getBytes(),
                null,
                user.getCity(),
                user.getState(),
                user.getZip(),
                user.getCountry(),
                user.getPhoneNumber().getBytes());
        return Response.ok(userEncDao.persist(enc)).build();
    }
}
