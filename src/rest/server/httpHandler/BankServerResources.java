package rest.server.httpHandler;

import bftsmart.tom.ServiceProxy;
import rest.server.model.ApplicationResponse;
import rest.server.model.CustomExtractor;
import rest.server.model.ExtractorMessage;
import rest.server.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * Implementacao do servidor de rendezvous em REST
 */
@Path("/users")
public class BankServerResources {

    public enum Operation {
        GET_ALL,
        GENERATE_MONEY,
        TRANSFER_MONEY
    }

    private ServiceProxy serviceProxy;
    private CustomExtractor ex;

    @SuppressWarnings("unchecked")
    BankServerResources(int port) {
        Comparator cmp = (Comparator<byte[]>) (o1, o2) -> Arrays.equals(o1, o2) ? 0 : -1;
        ex = new CustomExtractor();

        serviceProxy = new ServiceProxy(port == 8080 ? 0 : 1, null, cmp, ex);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public User[] listUsers() {
        try {
            byte[] reply = invokeOp(false, Operation.GET_ALL);

            if (reply.length > 0) {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                ApplicationResponse rs = (ApplicationResponse) objIn.readObject();

                if (rs.getStatusCode() != 200) {
                    throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
                } else {
                    Map<Long, User> body = (Map) rs.getBody();
                    return body.values().toArray(new User[0]);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
        }
        return new User[0];
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

            byte[] reply = ordered ? serviceProxy.invokeOrdered(byteOut.toByteArray()) : serviceProxy.invokeUnordered(byteOut.toByteArray());

            if (checkQuorum()) {
                return reply;
            } else {
                // No quorom
                throw new WebApplicationException("No quorum reached for request", Response.Status.PRECONDITION_FAILED);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
            return new byte[0];
        }
    }

    private boolean checkQuorum() {
        int numberOfReplicas = serviceProxy.getViewManager().getCurrentViewN();
        ExtractorMessage lastRound = ex.getLastRound();

        //TODO
        return lastRound.getTomMessages().length >= (numberOfReplicas / 2 + 1);
    }
}
