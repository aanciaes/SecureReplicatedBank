package rest.client;

import com.google.gson.Gson;
import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;
import rest.server.model.ReplicaResponse;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.util.Base64;

public class TransferClient {

    public static void transfer(WebTarget target, KeyPair kp, String toKey, Double amount) {

        try {
            String fromPubKString = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

            ClientTransferRequest clientRequest = new ClientTransferRequest();
            clientRequest.setFromPubKey(fromPubKString);
            clientRequest.setToPubKey(toKey);
            clientRequest.setAmount(amount);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(kp.getPrivate(), hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);

            Response response = target.path("/transfer").request().header("nonce", Utils.generateNonce())
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            //--- debug prints

            int status = response.getStatus();
            System.out.println("Response Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                System.out.println("Balance after transfer: " + clientResponse.getBody());

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


