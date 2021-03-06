package rest.client.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.Base64;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientResponse;
import rest.server.model.TypedValue;
import rest.server.model.WalletOperationType;
import rest.utils.Utils;

/**
 * Client that returns the balance of a user
 */
public class GetBalanceClient {

    private static Logger logger = LogManager.getLogger(GetBalanceClient.class.getName());


    /**
     * /**
     * Client that returns the balance of a user
     *
     * @param faults      Number of fault that the client wants to tolerate
     * @param target      WebTarget to the server
     * @param userKeyPair User public and private key
     * @param homoKey     Key used for homomorphic decryption (HomoAdd, HomoOpeInt)
     */
    @SuppressWarnings("Duplicates")
    public static synchronized void getBalance(WebTarget target, int faults, KeyPair userKeyPair, String homoKey) {
        try {
            String userKeyString = Base64.getEncoder().encodeToString(userKeyPair.getPublic().getEncoded());

            // Nonce to randomise message encryption
            long nonce = Utils.generateNonce();

            byte[] hashedMessage = Utils.hashMessage((userKeyString + nonce).getBytes());
            byte[] encryptedHash = Utils.encryptMessage("RSA", "SunJCE", userKeyPair.getPrivate(), hashedMessage);

            Response response = target
                    .path(String.format("/get/%s", URLEncoder.encode(userKeyString, "utf-8")))
                    .queryParam("signature", URLEncoder.encode(Base64.getEncoder().encodeToString(encryptedHash), "utf-8"))
                    .request()
                    .header("nonce", nonce)
                    .get();

            int status = response.getStatus();
            logger.debug("Get Balance Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.GET_BALANCE);

                if (conflicts > faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                } else {
                    //decrypt response if needed
                    TypedValue tv = new ObjectMapper().convertValue(clientResponse.getBody(), TypedValue.class);

                    switch (tv.getType()) {
                        case WALLET:
                            logger.info("Balance: " + tv.getAmountAsDouble());
                            break;

                        case HOMO_ADD:
                            PaillierKey paillierKey = (PaillierKey) HelpSerial.fromString(homoKey);
                            logger.info("Balance: " + HomoAdd.decrypt(tv.getAmountAsBigInteger(), paillierKey));
                            break;

                        case HOMO_OPE_INT:
                            HomoOpeInt ope = new HomoOpeInt(homoKey);
                            logger.info("Balance: " + ope.decrypt(tv.getAmountAsLong()));
                            break;

                        default:
                            logger.error("Received wrong data type");
                            break;
                    }
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
