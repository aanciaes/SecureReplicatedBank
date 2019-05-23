package rest.serverController.server;

import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientResponse;
import rest.server.model.ClientSumRequest;
import rest.server.model.ClientTransferRequest;
import rest.serverController.client.AdminServerRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Wallet Server API
 */
@Path("/manage")
public interface ManagerServer {

    @POST
    @Path("/up")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse upServer(@Context HttpHeaders headers, AdminServerRequest adminServerRequest);

    @POST
    @Path("/down")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse downServer(@Context HttpHeaders headers, AdminServerRequest adminServerRequest);
}
