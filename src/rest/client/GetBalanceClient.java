package rest.client;

import rest.server.model.ClientResponse;
import rest.server.model.ReplicaResponse;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.util.Base64;

public class GetBalanceClient {

    @SuppressWarnings("Duplicates")
    public static void getBalance(WebTarget target, KeyPair userKeyPair) {

        try {
            String userKeyString = Base64.getEncoder().encodeToString(userKeyPair.getPublic().getEncoded());

            // Nonce to randomise message encryption
            long nonce = Utils.generateNonce();

            byte[] hashedMessage = Utils.hashMessage((userKeyString + nonce).getBytes());
            byte[] encryptedHash = Utils.encryptMessage(userKeyPair.getPrivate(), hashedMessage);

            Response response = target
                    .path(String.format("/%s", URLEncoder.encode(userKeyString, "utf-8")))
                    .queryParam("signature", URLEncoder.encode(Base64.getEncoder().encodeToString(encryptedHash), "utf-8"))
                    .request()
                    .header("nonce", nonce)
                    .get();

            //--- debug prints

            int status = response.getStatus();
            System.out.println("Response Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                System.out.println("Current Balance: " + clientResponse.getBody());

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
