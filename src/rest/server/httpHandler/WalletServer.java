package rest.server.httpHandler;

import rest.server.model.ClientCreateRequest;
import rest.server.model.ClientResponse;
import rest.server.model.ClientSumRequest;
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
import rest.server.model.DataType;

/**
 * Wallet Server API
 */
@Path("/wallet")
public interface WalletServer {

    /**
     * List all users and its balances. It does not goes to a consensus, for debug purposes only
     *
     * @param headers Headers of request (nonce)
     * @return Client response with all users and balances and no replica reesponses
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse listUsers(@Context HttpHeaders headers);

    /**
     * Gets current balance of a user
     *
     * @param headers        Headers of the request
     * @param userIdentifier User to get the balance
     * @param signature      Signature of the request
     * @return Client response with the balance of the user with all replica responses
     */
    @GET
    @Path("/get/{userIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse getAmount(@Context HttpHeaders headers, @PathParam("userIdentifier") String userIdentifier, @QueryParam("signature") String signature);

    @GET
    @Path("/getbetween")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse getBetween(
            @Context HttpHeaders headers,
            @QueryParam("data_type") DataType dataType,
            @QueryParam("key_prf") String keyPrefix,
            @QueryParam("lowest") Long lowest,
            @QueryParam("highest") Long highest,
            @QueryParam("paillier_key") String paillierKey,
            @QueryParam("sym_key") String symKey
    );

    /**
     * Generates money for a user
     * Request must me made from a admin account.
     *
     * @param headers    Headers of request (nonce)
     * @param cliRequest Client request with the amount to be generated and the user that will get the money
     * @return Client response with the new balance of the user and with all replica responses
     */
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse createAccount(@Context HttpHeaders headers, ClientCreateRequest cliRequest);

    /**
     * Transfers money from one user to another
     *
     * @param headers    Headers of request (nonce)
     * @param cliRequest Client request with the amount to be transferred, the source and destination users
     * @return Client response with the new balance of the source user and with all replica responses
     */
    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse transferMoney(@Context HttpHeaders headers, ClientTransferRequest cliRequest);

    @POST
    @Path("/sum")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse sum (@Context HttpHeaders headers, ClientSumRequest clientSumRequest);

    @POST
    @Path("/set")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse setBalance (@Context HttpHeaders headers, ClientCreateRequest clientSetRequest);
}
