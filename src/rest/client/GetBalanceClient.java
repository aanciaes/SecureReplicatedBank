package rest.client;

import rest.server.model.ClientResponse;

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

            int status = response.getStatus();
            System.out.println("Get Balance Status: " + status);

            if (status == 200) {
                ClientResponse clientResponse = response.readEntity(ClientResponse.class);
                System.out.println("Current Balance: " + clientResponse.getBody());


                int maxConflicts = (Integer) (clientResponse.getResponses().size() / 2);

                int conflicts = Utils.verifyReplicaResponse(nonce, clientResponse);

                if (conflicts >= maxConflicts) {
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
