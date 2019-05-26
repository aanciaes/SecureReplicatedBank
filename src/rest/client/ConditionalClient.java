package rest.client;

import com.google.gson.Gson;
import hlib.hj.mlib.HomoAdd;
import java.math.BigInteger;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import rest.client.get.GetBalanceClient;
import rest.server.model.ClientConditionalUpd;
import rest.server.model.ClientResponse;
import rest.server.model.WalletOperationType;
import rest.utils.Update;
import rest.utils.Utils;

public class ConditionalClient {

    private static Logger logger = LogManager.getLogger(GetBalanceClient.class.getName());

    public static void conditional_upd(WebTarget target, int faults, String condKey, Double condValue, List<Update> conditionalUpdates, int condition) {
        try {
            Configurator.setLevel(Utils.class.getName(), Level.INFO);

            long nonce = Utils.generateNonce();
            ClientConditionalUpd clientRequest = new ClientConditionalUpd(condKey, condValue, conditionalUpdates, condition, nonce);

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);

            Response response = target.path("/conditional_upd").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            ClientResponse clientResponse = response.readEntity(ClientResponse.class);

            if (response.getStatus() == 200) {
                logger.debug("Amount Added: " + clientResponse.getBody());

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse, WalletOperationType.CONDITIONAL_UPD);

                if (conflicts > faults) {
                    logger.error("Conflicts found, operation is not accepted by the client");
                } else {
                    String res = (String) clientResponse.getBody();
                    System.out.println(res);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
