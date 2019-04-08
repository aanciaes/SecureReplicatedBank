package rest.client;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;
import rest.server.model.WalletOperationType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.util.Base64;

/**
 * Client that transfers the money from one user to another
 */
public class TransferClient {

    private static Logger logger = LogManager.getLogger(TransferClient.class.getName());

    /**
     * Client that transfers the money from one user to another
     *
     * @param target WebTarget to the server
     * @param kp     Public and private key of the user that wants to transfer money
     * @param toKey  Public key of the destniation user
     * @param amount Amount to transfer
     */
    public static void transfer(WebTarget target, int faults, KeyPair kp, String toKey, Double amount) {

        try {
            String fromPubKString = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

            ClientTransferRequest clientRequest = new ClientTransferRequest();
            clientRequest.setFromPubKey(fromPubKString);
            clientRequest.setToPubKey(toKey);
            clientRequest.setAmount(amount);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(kp.getPrivate(), hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);
            long nonce = Utils.generateNonce();
            Response response = target.path("/transfer").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            //--- debug prints

            int status = response.getStatus();
            logger.info("Transfer Money Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                logger.debug("Balance after transfer: " + clientResponse.getBody());

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.TRANSFER_MONEY);

                if (conflicts >= faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


