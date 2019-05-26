package rest.client.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import hlib.hj.mlib.HomoOpeInt;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientResponse;
import rest.server.model.DataType;
import rest.server.model.WalletOperationType;
import rest.sgx.model.GetBetweenResponse;
import rest.utils.Utils;

public class GetBetweenClient {

    private static Logger logger = LogManager.getLogger(GetBetweenClient.class.getName());

    /**
     * Get all keys with balance between amount.
     *
     * @param target    WebTarget to the server
     * @param faults    Number of fault that the client wants to tolerate
     * @param opeKey    Homo Ope Key for homomorphic decryption
     * @param dataType  Datatype for the request
     * @param lowest    lowest
     * @param highest   highest
     * @param keyPrefix Match keys with key prefix
     */
    public static void getBalanceBetween(WebTarget target, int faults, String opeKey, DataType dataType, Integer lowest, Integer highest, String keyPrefix) {
        try {
            // Nonce to randomise message encryption
            long nonce = Utils.generateNonce();

            Long requestLowest;
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
            logger.debug("Get Balance Between Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                String responseBodyAsString = (String) clientResponse.getBody();

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.GET_BETWEEN);

                if (conflicts > faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                } else {
                    GetBetweenResponse result = new ObjectMapper().readValue(responseBodyAsString, GetBetweenResponse.class);

                    logger.info("Keys between for datatype: " + dataType);
                    result.getResults().forEach(key -> {
                        logger.info("\t" + key);
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