package com.johnhite.sandbox.fle.resources;

import com.johnhite.sandbox.fle.hibernate.KeyDao;
import com.johnhite.sandbox.fle.hibernate.KeyEntity;
import com.johnhite.sandbox.fle.hibernate.UserKeyDao;
import com.johnhite.sandbox.fle.hibernate.UserKeyEntity;
import io.dropwizard.hibernate.UnitOfWork;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("keys")
@Produces({ MediaType.APPLICATION_JSON})
@Consumes({ MediaType.APPLICATION_JSON})
public class KeyResource {
    private KeyDao keyDao;
    private UserKeyDao userKeyDao;

    @Inject
    public KeyResource(KeyDao keyDao, UserKeyDao userKeyDao) {
        this.userKeyDao = userKeyDao;
        this.keyDao = keyDao;
    }

    @DELETE
    @Path("{idName}/{idValue}")
    @UnitOfWork
    public void deleteKey(@PathParam("idName") String idName, @PathParam("idValue") Long idValue) {
        byte[] id = UserKeyEntity.generateId(idValue, idName);
        Optional<UserKeyEntity> uke = userKeyDao.findById(id);
        if (uke.isPresent()) {
            long keyId = uke.get().getKeyId();
            KeyEntity k = keyDao.findById(keyId);
            keyDao.delete(k);
            userKeyDao.delete(uke.get());
        }
    }
}
