package rest.server.httpHandler;

import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;
import rest.server.model.User;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;

@Path("/wallet")
public interface WalletServer {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse listUsers(@Context HttpHeaders headers);

    @GET
    @Path("/{userIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse getAmount (@Context HttpHeaders headers, @PathParam("userIdentifier") String userIdentifier, @QueryParam("signature") String signature);

    @POST
    @Path("/generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse generateMoney(@Context HttpHeaders headers, ClientAddMoneyRequest cliRequest);

    @POST
    @Path("/transfer")
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse transferMoney(@Context HttpHeaders headers, ClientTransferRequest cliRequest);
}
