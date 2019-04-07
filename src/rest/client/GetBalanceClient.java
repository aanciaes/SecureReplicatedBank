package rest.client;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.util.KeyLoader;
import rest.server.model.ClientResponse;
import rest.server.model.ReplicaResponse;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Signature;
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

                int conflicts = 0;
                int maxConflicts = (Integer)(clientResponse.getResponses().size() / 2);

                for (ReplicaResponse replicaResponse : clientResponse.getResponses()) {
                    //TODO: Check signatures and nonces
                    if(nonce + 1 != replicaResponse.getNonce()){
                        conflicts++;
                        System.out.println("NONCE CONFLICT");
                    }
                    else if(!clientResponse.getBody().equals(replicaResponse.getBody()) ){
                        conflicts++;
                        System.out.println("AMOUNT CONFLICT");
                    }
                    else if(replicaResponse.getStatusCode() != 200){
                        conflicts++;
                        System.out.println("STATUS CONFLICT");
                    }else{
                        //DIFFERENT ALGORITHMS CHECK IT
                        KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
                        PublicKey pk = keyLoader.loadPublicKey(replicaResponse.getReplicaId());
                        Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
                        sig.initVerify(pk);
                        sig.update(Base64.getDecoder().decode(replicaResponse.getSerializedMessage()));
                        if (!sig.verify(Base64.getDecoder().decode(replicaResponse.getSignature()))) {
                            System.out.println("SIGNATURE CONFLICT");
                            conflicts++;
                        }
                    }
                }
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
