package rest.client;

import com.google.gson.Gson;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.PaillierKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class SumHomoAddClient {
    private static Logger logger = LogManager.getLogger(AddMoneyClient.class.getName());
    /**
     * Client that adds money to a user.
     *
     * @param target               WebTarget to the server
     * @param faults               Number of fault that the client wants to tolerate
     * @param amount               amount to add to the user
     */
    @SuppressWarnings("Duplicates")
    public static void sumMoney(WebTarget target, int faults, KeyPair kp, String amount, PaillierKey pk) {
        try {
            String toPubkString = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
            amount = HomoAdd.encrypt(new BigInteger(amount), pk).toString();

            ClientSumRequest clientRequest = new ClientSumRequest();
            clientRequest.setUserIdentifier(toPubkString);

            TypedValue clientTv = new TypedValue (amount, DataType.HOMO_ADD);
            clientRequest.setTypedValue(clientTv);
            clientRequest.setNsquare(pk.getNsquare().toString());

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(kp.getPrivate(), hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);
            long nonce = Utils.generateNonce();

            Response response = target.path("/sum").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            int status = response.getStatus();
            System.out.println(response.getStatusInfo().getReasonPhrase());
            logger.info("Sum Money Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                logger.debug("Amount summed: " + clientResponse.getBody());

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.HOMO_ADD_SUM);

                if (conflicts > faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                }else{
                    BigInteger responseAmount = HomoAdd.decrypt(clientRequest.getTypedValue().getAmountAsBigInteger(), pk);
                    System.out.println(clientRequest.getTypedValue().getAmountAsBigInteger());
                    System.out.println("Sum money: " + responseAmount);
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
