package rest.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientResponse;
import rest.server.model.WalletOperationType;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.Base64;


public class GetBalanceClient {

    private static Logger logger = LogManager.getLogger(GetBalanceClient.class.getName());

    @SuppressWarnings("Duplicates")
    public static void getBalance(WebTarget target, KeyPair userKeyPair) {
        try {
            String userKeyString = Base64.getEncoder().encodeToString(userKeyPair.getPublic().getEncoded());

            // Nonce to randomise message encryption
            long nonce = Utils.generateNonce();

            byte[] hashedMessage = Utils.hashMessage((userKeyString + nonce).getBytes());
            byte[] encryptedHash = Utils.encryptMessage(userKeyPair.getPrivate(), hashedMessage);

            Response response = target
                    .path(String.format("/%s", URLEncoder.encode(userKeyString, "utf-8")))
                    .queryParam("signature", URLEncoder.encode(Base64.getEncoder().encodeToString(encryptedHash), "utf-8"))
                    .request()
                    .header("nonce", nonce)
                    .get();

            int status = response.getStatus();
            logger.info("Get Balance Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                logger.info("Current Balance: " + clientResponse.getBody());

                int maxConflicts = clientResponse.getResponses().size() / 2;
                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.GET_BALANCE);

                if (conflicts >= maxConflicts) {
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
