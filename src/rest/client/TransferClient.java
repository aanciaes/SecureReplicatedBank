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


