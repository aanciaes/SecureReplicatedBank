package rest.client;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.core.TOMSender;
import bftsmart.tom.util.KeyLoader;
import com.google.gson.Gson;
import rest.server.model.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;

public class AddMoneyClient {
    @SuppressWarnings("Duplicates")
    public static void addMoney(WebTarget target, PrivateKey adminPrivateKey, PublicKey destinationPublicKey, Double amount) {

        try {
            String toPubkString = Base64.getEncoder().encodeToString(destinationPublicKey.getEncoded());

            ClientAddMoneyRequest clientRequest = new ClientAddMoneyRequest();
            clientRequest.setToPubKey(toPubkString);
            clientRequest.setAmount(amount);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(adminPrivateKey, hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);
            Long nonce = Utils.generateNonce();
            Response response = target.path("/generate").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            //--- debug prints

            int status = response.getStatus();
            System.out.println("Add Money Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                System.out.println("Amount Added: " + clientResponse.getBody());

                int maxConflicts = (Integer)(clientResponse.getResponses().size() / 2);

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse);

                if(conflicts >= maxConflicts){
                    System.out.println("CONFLICT FOUND!");
                }
            } else {
                System.out.println(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
