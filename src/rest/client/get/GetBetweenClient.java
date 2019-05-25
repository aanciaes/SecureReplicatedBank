package rest.client.get;

import hlib.hj.mlib.HomoOpeInt;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientResponse;
import rest.server.model.DataType;
import rest.server.model.WalletOperationType;
import rest.utils.Utils;

public class GetBetweenClient {

    private static Logger logger = LogManager.getLogger(GetBalanceClient.class.getName());

    public static void getBalanceBetween(WebTarget target, int faults, String opeKey, DataType dataType, Integer lowest, Integer highest, String keyPrefix) {
        try {
            // Nonce to randomise message encryption
            long nonce = Utils.generateNonce();

            long requestLowest;
            Long requestHighest;
            if (dataType == DataType.HOMO_OPE_INT) {
                HomoOpeInt ope = new HomoOpeInt(opeKey);
                requestLowest = ope.encrypt(lowest);
                requestHighest = ope.encrypt(highest);
            } else {
                requestLowest = lowest.longValue();
                requestHighest = highest.longValue();
            }

            Response response = target
                    .path("/getbetween")
                    .queryParam("data_type", dataType)
                    .queryParam("lowest", requestLowest)
                    .queryParam("highest", requestHighest)
                    .queryParam("key_prf", keyPrefix)
                    .request()
                    .header("nonce", nonce)
                    .get();

            int status = response.getStatus();
            logger.info("Get Balance Between Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                //new ObjectMapper().convertValue(clientResponse.getBody(), TypedValue.class);

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.GET_BETWEEN);

                if (conflicts > faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                } else {
                    List<String> keys = (ArrayList<String>) clientResponse.getBody();

                    keys.forEach(key -> {
                        logger.info("Key between: " + key);
                        System.out.println("Keys in between with dataType: " + dataType + ": " + key);
                    });
                }
            } else {
                logger.info(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}