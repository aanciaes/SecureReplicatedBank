package rest.client;

import com.google.gson.Gson;
import rest.server.model.ClientTransferRequest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class TransferClient {

    public static void main(String[] args) {

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/").build();
        WebTarget target = client.target(baseURI);

        try {
            KeyPair kp = Utils.generateNewKeyPair(1024);

            PublicKey pubk = kp.getPublic();
            PrivateKey privk = kp.getPrivate();

            String fromPubkString = Base64.getEncoder().encodeToString(pubk.getEncoded());
            String toPubkString = "asdasd";

            ClientTransferRequest clientRequest = new ClientTransferRequest();
            clientRequest.setFromPubKey(fromPubkString);
            clientRequest.setToPubKey(toPubkString);
            clientRequest.setAmount(200.00);

            // Nonce to randomise messaga encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(privk, hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);

            Response response = target.path("/wallet/transfer").request().header("nonce", Utils.generateNonce())
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            System.out.println(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


