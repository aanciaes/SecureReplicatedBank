package rest.client.create;

import com.google.gson.Gson;
import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.PaillierKey;
import java.math.BigInteger;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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

public class CreateHomoAddClient {

    private static Logger logger = LogManager.getLogger(CreateHomoAddClient.class.getName());

    /**
     * Creates a new account of HomoAdd Type
     *
     * @param target               Service url
     * @param faults               Number of faults that the client wants to tolerate
     * @param adminPrivateKey      Admin Private Key to sign the request
     * @param destinationPublicKey New User public key
     * @param amount               Initial amount of the new account
     * @param pk                   Paillier Key for homomorphic encryption
     */
    @SuppressWarnings("Duplicates")
    public static void createAccount(WebTarget target, int faults, PrivateKey adminPrivateKey, PublicKey destinationPublicKey, String amount, PaillierKey pk) {

        try {
            String toPubkString = Base64.getEncoder().encodeToString(destinationPublicKey.getEncoded());//pk.getNsquare().toString();
            amount = HomoAdd.encrypt(new BigInteger(amount), pk).toString();

            ClientCreateRequest clientRequest = new ClientCreateRequest();
            clientRequest.setToPubKey(toPubkString);

            String homoAddPaillierKey = HelpSerial.toString(pk);

            Key symKey = generateAES();

            byte[] encryptedHomoAddKeyBytes = Utils.encryptMessage("AES", "SunJCE", symKey, homoAddPaillierKey.getBytes());
            String encryptedPallierKey = Base64.getEncoder().encodeToString(encryptedHomoAddKeyBytes);

            byte[] encryptedSymKeyBytes = Utils.encryptMessage("RSA", "SunJCE", AdminSgxKeyLoader.loadPublicKey("sgxPublicKey.pem"), symKey.getEncoded());
            String encryptedSymKey = Base64.getEncoder().encodeToString(encryptedSymKeyBytes);

            TypedValue clientTv = new TypedValue(amount, DataType.HOMO_ADD, encryptedPallierKey, encryptedSymKey);
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
                    BigInteger responseAmount = HomoAdd.decrypt(clientRequest.getTypedValue().getAmountAsBigInteger(), pk);
                    logger.info("add money: " + responseAmount);
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Key generateAES() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // for example
            SecretKey secretKey = keyGen.generateKey();

            return secretKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
