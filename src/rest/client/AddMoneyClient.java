package rest.client;

import com.google.gson.Gson;
import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;
import rest.server.model.ReplicaResponse;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
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

            Response response = target.path("/generate").request().header("nonce", Utils.generateNonce())
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));



            int status = response.getStatus();
            System.out.println("Response Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                System.out.println("Amount Added: " + clientResponse.getBody());

                for (ReplicaResponse replicaResponse : clientResponse.getResponses()) {
                    //TODO: Check signatures and nonces
                    System.out.println("\t" + replicaResponse.getReplicaId());
                    System.out.println("\t" + replicaResponse.getStatusCode());
                    System.out.println("\t" + replicaResponse.getBody());
                    System.out.println();
                }
            } else {
                System.out.println(response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
