package rest.client;

import java.io.IOException;
import java.net.URI;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import rest.server.model.ClientTransferRequest;

public class Transfer {

    public static void main(String[] args) throws IOException {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/").build();

        WebTarget target = client.target(baseURI);

        try {
            KeyPairGenerator kpgen = KeyPairGenerator.getInstance("RSA");
            kpgen.initialize(1024);
            KeyPair kp = kpgen.generateKeyPair();

            PublicKey pubk = kp.getPublic();
            PrivateKey privk = kp.getPrivate();

            String fromPubkString = Base64.getEncoder().encodeToString(pubk.getEncoded());
            String toPubkString = "asdasd";

            ClientTransferRequest clientRequest = new ClientTransferRequest();
            clientRequest.setFromPubKey(fromPubkString);
            clientRequest.setToPubKey(toPubkString);
            clientRequest.setAmount(200.00);
            clientRequest.setNonce(generateNonce());

            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(clientRequest.getSerializeMessage().getBytes());
            byte[] hashMessage = digest.digest();

            Cipher c = Cipher.getInstance("RSA", "SunJCE");
            c.init(Cipher.ENCRYPT_MODE, privk);
            byte[] decryptedHash = c.doFinal(hashMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(decryptedHash));

            Response response = target.path("/wallet/transfer").request().post(Entity.entity(clientRequest, MediaType.APPLICATION_JSON));
            System.out.println(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static long generateNonce() {
        // create instance of SecureRandom class
        SecureRandom rand = new SecureRandom();

        return rand.nextLong();
    }
}


