package rest.sgx.server;

import rest.sgx.model.SGXClientSumRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import rest.sgx.model.SGXResponse;

@Path("/sgx")
public interface SGXServerInterface {

    @POST
    @Path("/sum")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SGXResponse sum(SGXClientSumRequest sgxClientRequest);

    @POST
    @Path("/compare")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void compare(SGXClientSumRequest sgxClientRequest);

    @POST
    @Path("/set")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void set_conditional(SGXClientSumRequest sgxClientRequest);

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void add_conditional(SGXClientSumRequest sgxClientRequest);
}
