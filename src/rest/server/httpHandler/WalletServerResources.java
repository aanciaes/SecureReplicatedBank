package rest.server.httpHandler;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.KeyLoader;
import rest.client.AdminKeyLoader;
import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;
import rest.server.model.CustomExtractor;
import rest.server.model.ReplicaResponse;
import rest.server.model.User;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Restfull resources of wallet server
 */
public class WalletServerResources implements WalletServer {

    private Logger logger = Logger.getLogger(WalletServerResources.class.getName());

    private ServiceProxy serviceProxy;
    private CustomExtractor extractor;

    @SuppressWarnings("unchecked")
    WalletServerResources(int port, int replicaId) {
        Comparator cmp = (Comparator<byte[]>) (o1, o2) -> Arrays.equals(o1, o2) ? 0 : -1;
        KeyLoader keyLoader = new RSAKeyLoader(replicaId, "config", false, "SHA512withRSA");
        extractor = new CustomExtractor();

        new ReplicaServer(replicaId);
        serviceProxy = new ServiceProxy(replicaId, null, cmp, extractor, keyLoader);
    }

    @Override
    public ClientResponse listUsers(HttpHeaders headers) {
        try {
            Long nonce = getNonceFromHeader(headers);

            byte[] reply = invokeOp(false, WalletOperationType.GET_ALL, nonce);
            List<ReplicaResponse> replicaResponseList = convertTomMessages(extractor.getRound((nonce + 1)).getTomMessages());

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
            } else {
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Exception putting value into map: " + e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ClientResponse getAmount(HttpHeaders headers, String userIdentifier, String signature) {
        System.err.printf("--- getting balance for user:for user: %s ---\n", userIdentifier);

        try {
            long nonce = getNonceFromHeader(headers);
            byte[] localHash = generateHash((userIdentifier + nonce).getBytes());
            byte[] signedHash = decryptRequest(generatePublicKeyFromString(userIdentifier), Base64.getDecoder().decode(signature));

            if (signedHash == null) {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }

            if (Arrays.equals(localHash, signedHash)) {
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
                return new ClientResponse(rs.getBody(), replicaResponses);
            } else {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ClientResponse generateMoney(HttpHeaders headers, ClientAddMoneyRequest cliRequest) {
        System.err.printf("--- generating: %f for user: %s ---\n", cliRequest.getAmount(), cliRequest.getToPubKey());

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
            return new ClientResponse(rs.getBody(), replicaResponses);

        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ClientResponse transferMoney(HttpHeaders headers, ClientTransferRequest cliRequest) {
        System.err.printf("--- transfering: %f from user: %s to user: %s\n", cliRequest.getAmount(), cliRequest.getFromPubKey(), cliRequest.getToPubKey());

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
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            throw new WebApplicationException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
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

    private long getNonceFromHeader(HttpHeaders headers) {
        MultivaluedMap<String, String> headerParams = headers.getRequestHeaders();
        return new Long(headerParams.get("nonce").get(0));
    }
}
