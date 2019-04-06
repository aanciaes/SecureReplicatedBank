package rest.client;

import com.google.gson.Gson;
import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientTransferRequest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.util.Base64;

public class AddMoneyClient {
    @SuppressWarnings("Duplicates")
    public static void addMoney(WebTarget target, KeyPair kp, Double amount) {

        try {
            String toPubkString = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

            ClientAddMoneyRequest clientRequest = new ClientAddMoneyRequest();
            clientRequest.setToPubKey(toPubkString);
            clientRequest.setAmount(amount);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(kp.getPrivate(), hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);

            Response response = target.path("/wallet/generate").request().header("nonce", Utils.generateNonce())
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            System.out.println(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
