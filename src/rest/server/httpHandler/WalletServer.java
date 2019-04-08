package rest.server.httpHandler;

import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("/wallet")
public interface WalletServer {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse listUsers(@Context HttpHeaders headers);

    @GET
    @Path("/{userIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse getAmount(@Context HttpHeaders headers, @PathParam("userIdentifier") String userIdentifier, @QueryParam("signature") String signature);

    @POST
    @Path("/generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse generateMoney(@Context HttpHeaders headers, ClientAddMoneyRequest cliRequest);

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse transferMoney(@Context HttpHeaders headers, ClientTransferRequest cliRequest);
}
