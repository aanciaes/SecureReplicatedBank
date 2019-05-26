package rest.client;

import com.google.gson.Gson;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.client.get.GetBalanceClient;
import rest.server.model.ClientConditionalUpd;
import rest.server.model.ClientResponse;
import rest.utils.Update;
import rest.utils.Utils;

public class ConditionalClient {

    private static Logger logger = LogManager.getLogger(GetBalanceClient.class.getName());

    public static void conditional_upd(WebTarget target, int faults, String condKey, Double condValue, List<Update> conditionalUpdates, int condition) {
        try {
            long nonce = Utils.generateNonce();
            ClientConditionalUpd clientRequest = new ClientConditionalUpd(condKey, condValue, conditionalUpdates, condition, nonce);

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);

            Response response = target.path("/conditional_upd").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            System.out.println(response.readEntity(ClientResponse.class).getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
