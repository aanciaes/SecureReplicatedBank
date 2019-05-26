package rest.client.create;

import com.google.gson.Gson;
import hlib.hj.mlib.HomoOpeInt;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.client.TestClient;
import rest.server.model.ClientCreateRequest;
import rest.server.model.ClientResponse;
import rest.server.model.DataType;
import rest.server.model.TypedValue;
import rest.server.model.WalletOperationType;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Utils;

public class CreateHomoOpeIntClient {

    private static Logger logger = LogManager.getLogger(CreateHomoOpeIntClient.class.getName());

    /**
     * Creates a new account of HomoAdd Type
     *
     * @param target               Service url
     * @param faults               Number of faults that the client wants to tolerate
     * @param adminPrivateKey      Admin Private Key to sign the request
     * @param destinationPublicKey New User public key
     * @param amount               Initial amount of the new account
     * @param homoKey              Key used for homomorphic homo ope int encryption
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

            TypedValue clientTv = new TypedValue(amount, DataType.HOMO_OPE_INT, Base64.getEncoder().encodeToString(encyptedPallietKey), null);
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
            logger.debug("Add Money Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                logger.debug("Amount Added: " + clientResponse.getBody());

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.CREATE_ACCOUNT);

                if (conflicts > faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                } else {
                    int responseAmount = ope.decrypt(clientRequest.getTypedValue().getAmountAsLong());
                    logger.info ("Amount added: " + responseAmount);
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
