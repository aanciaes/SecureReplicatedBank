package rest.client;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.util.KeyLoader;
import com.google.gson.Gson;
import rest.server.model.ClientResponse;
import rest.server.model.ClientTransferRequest;
import rest.server.model.ReplicaResponse;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Signature;
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
            long nonce = Utils.generateNonce();
            Response response = target.path("/transfer").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            //--- debug prints

            int status = response.getStatus();
            System.out.println("Transfer Money Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                System.out.println("Balance after transfer: " + clientResponse.getBody());

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


