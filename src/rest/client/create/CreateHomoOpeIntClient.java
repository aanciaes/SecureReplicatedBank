package rest.client.create;

import com.google.gson.Gson;
import hlib.hj.mlib.HomoOpeInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.*;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Utils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class CreateHomoOpeIntClient {

    private static Logger logger = LogManager.getLogger(CreateClient.class.getName());

    /**
     * Client that adds money to a user.
     *
     * @param target               WebTarget to the server
     * @param faults               Number of fault that the client wants to tolerate
     * @param adminPrivateKey      Admin Private Key to sign the request
     * @param destinationPublicKey Destination user public key
     * @param amount               amount to add to the user
     */
    @SuppressWarnings("Duplicates")
    public static void createAccount(WebTarget target, int faults, PrivateKey adminPrivateKey, PublicKey destinationPublicKey, String amount, String homoKey) {

        try {
            HomoOpeInt ope = new HomoOpeInt(homoKey);

            String toPubkString = Base64.getEncoder().encodeToString(destinationPublicKey.getEncoded());
            amount = String.valueOf(ope.encrypt(Integer.parseInt(amount)));

            ClientCreateRequest clientRequest = new ClientCreateRequest();
            clientRequest.setToPubKey(toPubkString);

            // Set homo key encrypted with sgx public key
            byte[] encyptedPallietKey = Utils.encryptMessage("RSA", "SunJCE", AdminSgxKeyLoader.loadPublicKey("sgxPublicKey.pem"), homoKey.getBytes());

            TypedValue clientTv = new TypedValue (amount, DataType.HOMO_OPE_INT, Base64.getEncoder().encodeToString(encyptedPallietKey), null);
            clientRequest.setTypedValue(clientTv);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage("RSA", "SunJCE", adminPrivateKey, hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);
            long nonce = Utils.generateNonce();
            Response response = target.path("/create").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            int status = response.getStatus();
            logger.info("Add Money Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                logger.debug("Amount Added: " + clientResponse.getBody());

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.CREATE_ACCOUNT);

                if (conflicts > faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                }else{
                    int responseAmount = ope.decrypt(clientRequest.getTypedValue().getAmountAsLong());
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
