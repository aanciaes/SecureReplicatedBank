package rest.server.httpHandler;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.KeyLoader;
import rest.server.model.ClientResponse;
import rest.server.model.CustomExtractor;
import rest.server.model.ReplicaResponse;
import rest.server.model.User;
import rest.server.model.WalletOperationType;
import rest.server.replica.ReplicaServer;

import javax.ws.rs.WebApplicationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Restfull resources of wallet server
 */
public class WalletServerResources implements WalletServer {

    private Logger logger = Logger.getLogger(WalletServerResources.class.getName());

    private ServiceProxy serviceProxy;
    private CustomExtractor exractor;

    @SuppressWarnings("unchecked")
    WalletServerResources(int port, int replicaId) {
        Comparator cmp = (Comparator<byte[]>) (o1, o2) -> Arrays.equals(o1, o2) ? 0 : -1;
        KeyLoader keyLoader = new RSAKeyLoader(replicaId, "config", false, "SHA512withRSA");
        exractor = new CustomExtractor();

        new ReplicaServer(replicaId);
        serviceProxy = new ServiceProxy(replicaId, null, cmp, exractor, keyLoader);
    }

    @Override
    public ClientResponse listUsers() {
        try {
            byte[] reply = invokeOp(false, WalletOperationType.GET_ALL, generateNonce());
            List<ReplicaResponse> replicaResponseList = convertTomMessages(exractor.getLastRound().getTomMessages());

            if (reply.length > 0) {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                ReplicaResponse rs = (ReplicaResponse) objIn.readObject();

                if (rs.getStatusCode() != 200) {
                    throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
                } else {
                    Map<Long, User> body = (Map) rs.getBody();
                    return new ClientResponse(body, replicaResponseList);//body.values().toArray(new User[0]);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        return new ClientResponse(null, null);
    }

    @Override
    public Double getAmount(Long id) {
        return null;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void generateMoney(Long id, Double amount) {
        System.err.printf("--- generating: %f for user: %s ---\n", amount, id);

        try {
            byte[] reply = invokeOp(true, WalletOperationType.GENERATE_MONEY, id, amount, generateNonce());

            if (reply.length > 0) {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                ReplicaResponse rs = (ReplicaResponse) objIn.readObject();

                if (rs.getStatusCode() != 200) {
                    throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
                }
            }
        } catch (IOException |
                ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void transferMoney(Long id, Double amount, Long destination) {
        System.err.printf("--- transfering: %f from user: %d to user: %d\n", amount, id, destination);

        try {
            byte[] reply = invokeOp(true, WalletOperationType.TRANSFER_MONEY, id, amount, destination, generateNonce());

            if (reply.length > 0) {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                ReplicaResponse rs = (ReplicaResponse) objIn.readObject();

                if (rs.getStatusCode() != 200) {
                    throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
                }
            }
        } catch (IOException |
                ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private byte[] invokeOp(boolean ordered, WalletOperationType operation, Object... args) {
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
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            return new byte[0];
        }
    }

    private List<ReplicaResponse> convertTomMessages(TOMMessage[] tomMessages) {
        List<ReplicaResponse> replicaResponseList = new ArrayList<ReplicaResponse>();
        for (TOMMessage tomMessage : tomMessages) {
            byte[] content = tomMessage.getContent();
            try {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(content);
                ObjectInput objIn = new ObjectInputStream(byteIn);
                ReplicaResponse rs = (ReplicaResponse) objIn.readObject();

                // Set serialized message and signature to client response
                rs.setReplicaId(tomMessage.getSender());
                rs.setSerializedMessage(tomMessage.serializedMessage);
                rs.setSignature(tomMessage.serializedMessageSignature);

                replicaResponseList.add(rs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return replicaResponseList;
    }

    private long generateNonce() {
        // create instance of SecureRandom class
        SecureRandom rand = new SecureRandom();

        return rand.nextLong();
    }
}
