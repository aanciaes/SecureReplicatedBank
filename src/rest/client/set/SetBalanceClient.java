package rest.client.set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Base64;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientCreateRequest;
import rest.server.model.ClientResponse;
import rest.server.model.DataType;
import rest.server.model.TypedValue;
import rest.server.model.WalletOperationType;
import rest.utils.Utils;

public class SetBalanceClient {
    private static Logger logger = LogManager.getLogger(SetBalanceClient.class.getName());

    /**
     * Client that returns the balance of a user
     *
     * @param faults   Number of fault that the client wants to tolerate
     * @param target   WebTarget to the server
     * @param kp       User public and private key
     * @param homoKey  Key used for homomorphic decryption (HomoAdd, HomoOpeInt)
     * @param amount   Amount to set to
     * @param dataType Datatype of the request
     */
    @SuppressWarnings("Duplicates")
    public static synchronized void setBalance(WebTarget target, int faults, KeyPair kp, String homoKey, String amount, DataType dataType) {
        try {
            String toPubkString = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
            switch (dataType) {
                case HOMO_ADD:
                    PaillierKey paillierKey = (PaillierKey) HelpSerial.fromString(homoKey);
                    amount = HomoAdd.encrypt(new BigInteger(amount), paillierKey).toString();
                    break;
                case HOMO_OPE_INT:
                    HomoOpeInt ope = new HomoOpeInt(homoKey);
                    amount = String.valueOf(ope.encrypt(Integer.parseInt(amount)));
            }
            ClientCreateRequest clientSetRequest = new ClientCreateRequest();
            clientSetRequest.setToPubKey(toPubkString);

            TypedValue clientTv = new TypedValue(amount, dataType, null, null);
            clientSetRequest.setTypedValue(clientTv);

            // Nonce to randomise message encryption
            clientSetRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientSetRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage("RSA", "SunJCE", kp.getPrivate(), hashedMessage);

            clientSetRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientSetRequest);
            long nonce = Utils.generateNonce();

            Response response = target.path("/set").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));


            int status = response.getStatus();
            logger.debug("Set Balance Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.SET_BALANCE);

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
