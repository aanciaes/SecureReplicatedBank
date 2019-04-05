package rest.server.httpHandler;

import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;
import rest.server.model.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/wallet")
public interface WalletServer {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse listUsers();

    @GET
    @Path("/{id}")
    Double getAmount (@PathParam("id") Long id);

    @POST
    @Path("/{id}/generate")
    void generateMoney(@PathParam("id") Long id, @QueryParam("amount") Double amount);

    @POST
    @Path("/transfer")
    @Consumes (MediaType.APPLICATION_JSON)
    void transferMoney(ClientTransferRequest cliRequest);
}
