package rest.server.httpHandler;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.KeyLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.client.AdminKeyLoader;
import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;
import rest.server.model.CustomExtractor;
import rest.server.model.ReplicaResponse;
import rest.server.model.WalletOperationType;
import rest.server.replica.ReplicaServer;

import javax.crypto.Cipher;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Restful resources of wallet server
 */
public class WalletServerResources implements WalletServer {

    private static Logger logger = LogManager.getLogger(WalletServerResources.class.getName());

    private ServiceProxy serviceProxy;
    private CustomExtractor extractor;
    private boolean unpredictable;
    private ReplicaServer replicaServer;

    @SuppressWarnings("unchecked")
    WalletServerResources(int replicaId, boolean unpredictable) {
        //Default comparator
        Comparator cmp = (Comparator<byte[]>) (o1, o2) -> Arrays.equals(o1, o2) ? 0 : -1;

        //Default key loader
        KeyLoader keyLoader = new RSAKeyLoader(replicaId, "config", false, "SHA512withRSA");
        extractor = new CustomExtractor();

        this.unpredictable = unpredictable;
        replicaServer = new ReplicaServer(replicaId, unpredictable);
        serviceProxy = new ServiceProxy(replicaId, null, cmp, extractor, keyLoader);
    }

    //For debug purposes only
    @Override
    public ClientResponse listUsers(HttpHeaders headers) {
        return new ClientResponse(replicaServer.getAllNoConsensus(), null);//body.values().toArray(new User[0]);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ClientResponse getAmount(HttpHeaders headers, String userIdentifier, String signature) {
        logger.info(String.format("getting balance for user:for user: %s ---", userIdentifier));

        try {
            long nonce = getNonceFromHeader(headers);
            byte[] localHash = generateHash((userIdentifier + nonce).getBytes());
            byte[] signedHash = decryptRequest(generatePublicKeyFromString(userIdentifier), Base64.getDecoder().decode(signature));

            if (signedHash == null) {
                //hash mismatch. A user is trying to get the balance of another user
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }

            if (Arrays.equals(localHash, signedHash)) {
                //hash mismatch. A user is trying to get the balance of another user
                byte[] reply = invokeOp(
                        false,
                        WalletOperationType.GET_BALANCE,
                        userIdentifier,
                        nonce
                );
                // Reply from the replicas
                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                ReplicaResponse rs = (ReplicaResponse) objIn.readObject();

                if (rs.getStatusCode() != 200) {
                    throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
                }

                List<ReplicaResponse> replicaResponses = convertTomMessages(extractor.getRound((nonce + 1)).getTomMessages());

                return forceErrorForClient(new ClientResponse(rs.getBody(), replicaResponses));
            } else {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ClientResponse generateMoney(HttpHeaders headers, ClientAddMoneyRequest cliRequest) {
        logger.info(String.format("generating: %f for user: %s ---", cliRequest.getAmount(), cliRequest.getToPubKey()));

        try {
            byte[] hashMessage = generateHash(cliRequest.getSerializeMessage().getBytes());
            PublicKey fromPublicKey = AdminKeyLoader.loadPublicKey(); // Only admin user can perform this operation
            byte[] decryptedHash = decryptRequest(fromPublicKey, Base64.getDecoder().decode(cliRequest.getSignature()));

            // Could not decrypt hash from message
            if (decryptedHash == null) {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }
            // Comparing hashes. If not equal, message has been tampered with
            if (!Arrays.equals(hashMessage, decryptedHash)) {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }

            Long nonce = getNonceFromHeader(headers);
            byte[] reply = invokeOp(
                    true,
                    WalletOperationType.GENERATE_MONEY,
                    cliRequest,
                    nonce
            );

            // Reply from the replicas
            ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
            ObjectInput objIn = new ObjectInputStream(byteIn);

            ReplicaResponse rs = (ReplicaResponse) objIn.readObject();

            if (rs.getStatusCode() != 200) {
                throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
            }

            List<ReplicaResponse> replicaResponses = convertTomMessages(extractor.getRound((nonce + 1)).getTomMessages());
            return forceErrorForClient(new ClientResponse(rs.getBody(), replicaResponses));

        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ClientResponse transferMoney(HttpHeaders headers, ClientTransferRequest cliRequest) {
        logger.info(String.format("transfering: %f from user: %s to user: %s", cliRequest.getAmount(), cliRequest.getFromPubKey(), cliRequest.getToPubKey()));

        try {
            byte[] hashMessage = generateHash(cliRequest.getSerializeMessage().getBytes());
            PublicKey fromPublicKey = generatePublicKeyFromString(cliRequest.getFromPubKey());
            byte[] decryptedHash = decryptRequest(fromPublicKey, Base64.getDecoder().decode(cliRequest.getSignature()));

            if (decryptedHash == null) {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }

            if (Arrays.equals(hashMessage, decryptedHash)) {

                Long nonce = getNonceFromHeader(headers);
                byte[] reply = invokeOp(
                        true,
                        WalletOperationType.TRANSFER_MONEY,
                        cliRequest,
                        nonce
                );

                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                ReplicaResponse rs = (ReplicaResponse) objIn.readObject();

                if (rs.getStatusCode() != 200) {
                    throw new WebApplicationException(rs.getMessage(), rs.getStatusCode());
                } else {
                    return new ClientResponse(rs.getBody(), convertTomMessages(extractor.getRound((nonce + 1)).getTomMessages()));
                }

            } else {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Invokes an operation to the replicas
     *
     * @param ordered   If the operation needs to be ordered to the state machine or not
     * @param operation Operation type that is being performed
     * @param args      Arguments to send to the replicas
     * @return The response from the replica
     */
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
            logger.error("Exception putting value into map: " + e.getMessage());
            return new byte[0];
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Converts the replica responses from tomMessage to a more controlled class
     * Sets the replica Id for that message, and its serialization and signature
     *
     * @param tomMessages Tom messages to be parses
     * @return List of replica responses
     */
    private List<ReplicaResponse> convertTomMessages(TOMMessage[] tomMessages) {
        List<ReplicaResponse> replicaResponseList = new ArrayList<ReplicaResponse>();
        for (TOMMessage tomMessage : tomMessages) {
            if (tomMessage != null) {
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
        }
        return replicaResponseList;
    }

    /**
     * Generates an hash for a message with SHA-512 algorithm
     *
     * @param toHash Message to hash
     * @return Hashed message
     */
    private byte[] generateHash(byte[] toHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(toHash);
            return digest.digest();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a class java.security.PublicKey from a string
     *
     * @param key Key in string format
     * @return java.security.PublicKey
     */
    private PublicKey generatePublicKeyFromString(String key) {
        try {
            byte[] byteKey = Base64.getDecoder().decode(key);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts a message with a given key
     *
     * @param pubk The key to decrypt the message
     * @param data The data to be decrypted
     * @return data in plain text
     */
    private byte[] decryptRequest(PublicKey pubk, byte[] data) {
        try {
            Cipher c = Cipher.getInstance("RSA", "SunJCE");
            c.init(Cipher.DECRYPT_MODE, pubk);
            return c.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract nonce from the request headers
     *
     * @param headers Headers of request
     * @return Long passed from the client
     */
    private long getNonceFromHeader(HttpHeaders headers) {
        MultivaluedMap<String, String> headerParams = headers.getRequestHeaders();
        return new Long(headerParams.get("nonce").get(0));
    }

    /**
     * Forces the server to return an error if in unpredictable mode
     *
     * @param clientResponse Client response to be returned to the user
     * @return If in unpredictable mode, client response with forced errors. If not, the same client response as passed in the arguments
     */
    private ClientResponse forceErrorForClient(ClientResponse clientResponse) {
        if (timeForError()) {
            switch (errorType()) {
                case 0:
                    logger.debug("forcing error - wrong amount on response");
                    clientResponse.setBody(123.0);
                    break;
                case 1:
                    logger.debug("forcing error - wrong operation type on replica response");
                    clientResponse.getResponses().get(0).setOperationType(WalletOperationType.TRANSFER_MONEY);
                    break;
                case 2:
                    logger.debug("forcing error - wrong signature on replica response");
                    clientResponse.getResponses().get(0).setSignature(new byte[0]);
                    break;
                case 3:
                    logger.debug("forcing error - wrong nonce on replica response");
                    clientResponse.getResponses().get(0).setNonce(123456789L);
                    break;
                case 4:
                    logger.debug("forcing error - wrong status code on replica response");
                    clientResponse.getResponses().get(0).setStatusCode(123);
                    break;
            }
        }

        return clientResponse;
    }

    /**
     * Computes if it is time to force an error
     * If in unpredictable mode, errors have a 10 percent chance to occur
     *
     * @return Force error or not based on unpredictable mode and probability
     */
    private boolean timeForError() {
        if (unpredictable) {

            Random r = new Random();
            int low = 0;
            int high = 9;
            int result = r.nextInt(high - low) + low;

            // 10% of probability of error
            return result == 0;
        } else {
            return false;
        }
    }

    /**
     * Randomises a forced error type
     *
     * @return Integer representing a forced error type
     */
    private int errorType() {
        Random r = new Random();
        int low = 0;
        int high = 4;

        return r.nextInt(high - low) + low;
    }
}
