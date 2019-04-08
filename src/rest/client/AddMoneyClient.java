package rest.client;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientResponse;
import rest.server.model.WalletOperationType;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class AddMoneyClient {

    private static Logger logger = LogManager.getLogger(AddMoneyClient.class.getName());

    @SuppressWarnings("Duplicates")
    public static void addMoney(WebTarget target, PrivateKey adminPrivateKey, PublicKey destinationPublicKey, Double amount) {

        try {
            String toPubkString = Base64.getEncoder().encodeToString(destinationPublicKey.getEncoded());

            ClientAddMoneyRequest clientRequest = new ClientAddMoneyRequest();
            clientRequest.setToPubKey(toPubkString);
            clientRequest.setAmount(amount);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(adminPrivateKey, hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);
            Long nonce = Utils.generateNonce();
            Response response = target.path("/generate").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            //--- debug prints

            int status = response.getStatus();
            logger.info("Add Money Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                logger.debug("Amount Added: " + clientResponse.getBody());

                int maxConflicts = clientResponse.getResponses().size() / 2;

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.GENERATE_MONEY);

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
