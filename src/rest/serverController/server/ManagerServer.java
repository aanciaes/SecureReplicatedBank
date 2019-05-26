package rest.serverController.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import rest.serverController.model.AdminServerRequest;

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
