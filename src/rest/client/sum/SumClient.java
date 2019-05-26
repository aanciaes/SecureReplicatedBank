package rest.client.sum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.client.create.CreateClient;
import rest.server.model.*;
import rest.utils.Utils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Base64;

public class SumClient {
    private static Logger logger = LogManager.getLogger(CreateClient.class.getName());
    /**
     * Client that adds money to a user.
     *
     * @param target               WebTarget to the server
     * @param faults               Number of fault that the client wants to tolerate
     * @param amount               amount to add to the user
     */
    @SuppressWarnings("Duplicates")
    public static void sumMoney(WebTarget target, int faults, KeyPair kp, DataType dataType, String amount, String key) {
        try {
            String toPubkString = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

            ClientSumRequest clientRequest = new ClientSumRequest();
            clientRequest.setUserIdentifier(toPubkString);

            PaillierKey paillierKey = null;
            HomoOpeInt homoOpeInt = null;

            if (dataType == DataType.HOMO_ADD) {
                paillierKey = (PaillierKey) HelpSerial.fromString(key);
                amount = HomoAdd.encrypt(new BigInteger(amount), paillierKey).toString();
                clientRequest.setNsquare(paillierKey.getNsquare().toString());
            }else if(dataType == DataType.HOMO_OPE_INT){
                homoOpeInt = new HomoOpeInt(key);
                amount = ((Long) homoOpeInt.encrypt(Integer.parseInt(amount))).toString();
            }

            TypedValue clientTv = new TypedValue (amount, dataType, null, null);
            clientRequest.setTypedValue(clientTv);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage("RSA", "SunJCE", kp.getPrivate(), hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);
            long nonce = Utils.generateNonce();

            Response response = target.path("/sum").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            int status = response.getStatus();
            logger.info("Sum Money Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                TypedValue responseValue = new ObjectMapper().convertValue(clientResponse.getBody(), TypedValue.class);

                logger.debug("Amount summed: " + responseValue.getAmount());

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.SUM);

                if (conflicts > faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                }else{
                    String responseAmount = responseValue.getAmount();
                    if (dataType == DataType.HOMO_ADD) {
                        responseAmount = HomoAdd.decrypt(responseValue.getAmountAsBigInteger(), paillierKey).toString();
                    }
                    if (dataType == DataType.HOMO_OPE_INT) {
                        responseAmount = ((Integer)homoOpeInt.decrypt(responseValue.getAmountAsLong())).toString();
                    }

                    System.out.println("Balance after sum: " + responseAmount);
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
