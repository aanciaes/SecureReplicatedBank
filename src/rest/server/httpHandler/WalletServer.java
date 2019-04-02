package rest.server.httpHandler;

import rest.server.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/wallet")
public interface WalletServer {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    User[] listUsers();

    @GET
    @Path("/{id}")
    Double getAmount (@PathParam("id") Long id);

    @POST
    @Path("/{id}/generate")
    void generateMoney(@PathParam("id") Long id, @QueryParam("amount") Double amount);

    @POST
    @Path("/{id}/transfer")
    void transferMoney(@PathParam("id") Long id, @QueryParam("amount") Double amount, @QueryParam("destination") Long destination);
}
