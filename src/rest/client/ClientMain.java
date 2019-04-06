package rest.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ClientMain {

    private static List<String> users = new ArrayList<String>();

    public static void main(String[] args) {

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/").build();
        WebTarget target = client.target(baseURI);
        int nUsers = 0;
        while(nUsers < 1){
            try {
                KeyPair kp = Utils.generateNewKeyPair(1024);
                users.add(Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
                AddMoneyClient.addMoney(target, AdminKeyLoader.loadPrivateKey(), kp.getPublic(), 200.0);
                nUsers++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(users);
    }
}
