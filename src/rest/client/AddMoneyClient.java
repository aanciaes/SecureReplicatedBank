package rest.client;

import java.net.URI;
import java.security.KeyPair;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client that adds money
 */
public class AddMoneyClient {

    private static Logger logger = LogManager.getLogger(AddMoneyClient.class.getName());

    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "client.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "qwerty");

            int faults = 1;

            Client client = ClientBuilder.newBuilder()
                    .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                    .build();

            URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/wallet/").build();
            WebTarget target = client.target(baseURI);

            KeyPair kp = Utils.generateNewKeyPair(1024);

            //AddMoneyWalletClient.addMoney(target, faults, AdminKeyLoader.loadPrivateKey(), kp.getPublic(), "1000");
            //AddMoneyHomoAddClient.addMoney(target, faults, AdminKeyLoader.loadPrivateKey(), kp.getPublic(), "1000");
            AddMoneyHomoOpeIntClient.addMoney(target, faults, AdminKeyLoader.loadPrivateKey(), kp.getPublic(), "1000");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
