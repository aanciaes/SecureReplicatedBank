package rest.client;

import com.google.gson.Gson;

import java.math.BigInteger;
import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.PaillierKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientAddMoneyRequest;
import rest.server.model.ClientResponse;
import rest.server.model.DataType;
import rest.server.model.TypedValue;
import rest.server.model.WalletOperationType;

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
