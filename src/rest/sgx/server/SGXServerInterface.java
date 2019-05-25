package rest.sgx.server;
import rest.sgx.model.SGXClientRequest;
import rest.sgx.model.TypedKey;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/sgx")
public interface SGXServerInterface {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, TypedKey> listUsers();

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void create(@Context HttpHeaders headers, SGXClientRequest sgxClientRequest);

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
