package com.johnhite.sandbox.fle.resources;

import com.johnhite.sandbox.fle.hibernate.UserDao;
import com.johnhite.sandbox.fle.hibernate.UserEntity;
import io.dropwizard.hibernate.UnitOfWork;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("users")
@Produces({ MediaType.APPLICATION_JSON})
@Consumes({ MediaType.APPLICATION_JSON})
public class UserResource {
    private UserDao userDao;

    @Inject
    public UserResource(UserDao userDao) {
        this.userDao = userDao;
    }

    @Path("{id}")
    @GET
    @UnitOfWork(readOnly = true)
    public Response getById(@PathParam("id") long id) {
        return Response.ok(userDao.findById(id)).build();
    }

    @POST
    @UnitOfWork
    public Response createUser( UserEntity user) {
        return Response.ok(userDao.persist(user)).build();
    }
}
