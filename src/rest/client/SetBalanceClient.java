package rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
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
import java.util.Base64;

public class SetBalanceClient {
    private static Logger logger = LogManager.getLogger(GetBalanceClient.class.getName());

    /**
     * Client that returns the balance of a user
     *
     * @param faults      Number of fault that the client wants to tolerate
     * @param target      WebTarget to the server
     * @param kp User public and private key
     */
    @SuppressWarnings("Duplicates")
    public static void setBalance(WebTarget target, int faults, KeyPair kp, String homoKey, String amount, DataType dataType) {
        try {
            String toPubkString = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
            switch(dataType){
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

            TypedValue clientTv = new TypedValue (amount, dataType);
            clientSetRequest.setTypedValue(clientTv);

            // Nonce to randomise message encryption
            clientSetRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientSetRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(kp.getPrivate(), hashedMessage);

            clientSetRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientSetRequest);
            long nonce = Utils.generateNonce();

            Response response = target.path("/set").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));


            int status = response.getStatus();
            logger.info("Set Balance Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                logger.info("Current Balance: " + clientResponse.getBody());

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
                            System.out.println("Balance: " + HomoAdd.decrypt(tv.getAmountAsBigInteger(), paillierKey));
                            break;

                        case HOMO_OPE_INT:
                            HomoOpeInt ope = new HomoOpeInt(homoKey);
                            logger.info("Balance: " + ope.decrypt(tv.getAmountAsLong()));
                            System.out.println("Balance: " + ope.decrypt(tv.getAmountAsLong()));
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
