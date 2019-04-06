package rest.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ClientMain {

    private static List<KeyPair> users = new ArrayList();

    public static void main(String[] args) {

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/wallet/").build();
        WebTarget target = client.target(baseURI);
        int nUsers = 0;
        while(nUsers < 1){
            try {
                KeyPair kp = Utils.generateNewKeyPair(1024);
                users.add(kp);
                AddMoneyClient.addMoney(target, AdminKeyLoader.loadPrivateKey(), kp.getPublic(), -1000.0);
                nUsers++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //TransferClient.transfer(target,users.get(0),Base64.getEncoder().encodeToString(users.get(1).getPublic().getEncoded()), 200.0);
    }
}
