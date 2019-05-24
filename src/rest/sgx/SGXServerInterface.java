package rest.sgx;

import rest.server.model.SGXClientRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("/sgx")
public interface SGXServerInterface {

    @POST
    @Path("/sum")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void sum(@Context HttpHeaders headers, SGXClientRequest sgxClientRequest);

    @POST
    @Path("/compare")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void compare(@Context HttpHeaders headers, SGXClientRequest sgxClientRequest);

    @POST
    @Path("/set")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void set_conditional(@Context HttpHeaders headers, SGXClientRequest sgxClientRequest);

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void add_conditional(@Context HttpHeaders headers, SGXClientRequest sgxClientRequest);
}
