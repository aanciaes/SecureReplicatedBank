package rest.sgx.server;

import rest.sgx.model.SGXClientSumRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import rest.sgx.model.SGXConditionalUpdateRequest;
import rest.sgx.model.SGXGetBetweenRequest;
import rest.sgx.model.SGXResponse;

@Path("/sgx")
public interface SGXServerInterface {

    @POST
    @Path("/sum")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SGXResponse sum(SGXClientSumRequest sgxClientRequest);

    @POST
    @Path("/getBetween")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SGXResponse getBetween(SGXGetBetweenRequest sgxClientRequest);

    @POST
    @Path("/conditional_upd")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void compare(SGXConditionalUpdateRequest sgxClientRequest);

}
