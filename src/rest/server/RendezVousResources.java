package rest.server;

import bftsmart.tom.ServiceProxy;
import rest.server.model.ApplicationResponse;
import rest.server.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Implementacao do servidor de rendezvous em REST
 */
@Path("/users")
public class RendezVousResources {

    private Map<Long, User> db = new ConcurrentHashMap<>();

    private ReplicaServer replicaServer;
    private ServiceProxy serviceProxy;

    public enum Operation {
        GENERATE_MONEY,
        TRANSFER_MONEY
    }

    RendezVousResources(int port) {
        replicaServer = new ReplicaServer(port == 8080 ? 0 : 1);
        serviceProxy = new ServiceProxy(port == 8080 ? 0 : 1, null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public User[] endpoints() {
        return replicaServer.listUsers();
    }

    @POST
    @Path("/{id}/generate")
    @SuppressWarnings("Duplicates")
    public void generateMoney(@PathParam("id") Long id, @QueryParam("amount") Double amount) {
        System.err.printf("--- generating: %f for user: %s\n", amount, id);

        try {
            byte[] reply = invokeOp(true, Operation.GENERATE_MONEY, id, amount);

            if (reply.length > 0) {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                ApplicationResponse rs = (ApplicationResponse) objIn.readObject();

                if (rs.getStatusCode() != 200) {
                    throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
                }
            }

        } catch (IOException |
                ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
    }

    @POST
    @Path("/{id}/transfer")
    @SuppressWarnings("Duplicates")
    public void transferMoney(@PathParam("id") Long id, @QueryParam("amount") Double amount, @QueryParam("destination") Long destination) {
        System.err.printf("--- transfering: %f from user: %d to user: %d\n", amount, id, destination);

        try {
            byte[] reply = invokeOp(true, Operation.TRANSFER_MONEY, id, amount, destination);

            if (reply.length > 0) {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                ApplicationResponse rs = (ApplicationResponse) objIn.readObject();

                if (rs.getStatusCode() != 200) {
                    throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
                }
            }

        } catch (IOException |
                ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
    }

    private byte[] invokeOp(boolean ordered, Operation operation, Object... args) {
        try (
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(operation);

            for (Object argument : args) {
                objOut.writeObject(argument);
            }

            objOut.flush();
            byteOut.flush();

            return ordered ? serviceProxy.invokeOrdered(byteOut.toByteArray()) : serviceProxy.invokeUnordered(byteOut.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
            return new byte[0];
        }
    }
}
