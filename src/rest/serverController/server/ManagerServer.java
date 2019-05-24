package rest.serverController.server;

import rest.server.model.ClientResponse;
import rest.serverController.model.AdminServerRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Wallet Server API
 */
@Path("/manager")
public interface ManagerServer {

    @POST
    @Path("/up")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void upServer(@Context HttpHeaders headers, AdminServerRequest adminServerRequest);

    @POST
    @Path("/down")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void downServer(@Context HttpHeaders headers, AdminServerRequest adminServerRequest);
}
