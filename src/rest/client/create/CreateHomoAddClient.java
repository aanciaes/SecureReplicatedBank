package rest.client.create;

import com.google.gson.Gson;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.PaillierKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.*;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Utils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class CreateHomoAddClient {

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
    public static void createAccount(WebTarget target, int faults, PrivateKey adminPrivateKey, PublicKey destinationPublicKey, String amount, PaillierKey pk) {

        try {
            String toPubkString = Base64.getEncoder().encodeToString(destinationPublicKey.getEncoded());//pk.getNsquare().toString();
            amount = HomoAdd.encrypt(new BigInteger(amount), pk).toString();

            ClientCreateRequest clientRequest = new ClientCreateRequest();
            clientRequest.setToPubKey(toPubkString);

            TypedValue clientTv = new TypedValue (amount, DataType.HOMO_ADD);
            clientRequest.setTypedValue(clientTv);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(adminPrivateKey, hashedMessage);

            byte[] encyptedPallietKey = Utils.encryptMessage(AdminSgxKeyLoader.loadPublicKey("sgxPublicKey.pem"), pk.toString().getBytes());

            clientRequest.setEncryptedKey(Base64.getEncoder().encodeToString(encyptedPallietKey));
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
                    BigInteger responseAmount = HomoAdd.decrypt(clientRequest.getTypedValue().getAmountAsBigInteger(), pk);
                    System.out.println(clientRequest.getTypedValue().getAmountAsBigInteger());
                    System.out.println("add money: " + responseAmount);
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
