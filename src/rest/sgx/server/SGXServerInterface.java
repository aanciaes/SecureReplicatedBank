package rest.sgx.server;

import rest.sgx.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
    @Path("/check_condition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    boolean compare(SGXConditionalUpdateRequest sgxClientRequest);

    @POST
    @Path("/applyConditionUpdate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SGXResponse applyConditionUpdate (SGXApplyUpdateRequest sgxApplyUpdateRequest);
}
