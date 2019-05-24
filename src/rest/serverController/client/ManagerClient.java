package rest.serverController.client;

import com.google.gson.Gson;
import org.omg.CORBA.INTERNAL;
import rest.client.AdminKeyLoader;
import rest.client.Utils;
import rest.serverController.model.AdminServerRequest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ManagerClient {

    public static void main(String[] args) {

        try {
            System.setProperty("javax.net.ssl.trustStore", "client.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "qwerty");

            Client client = ClientBuilder.newBuilder()
                    .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                    .build();

            URI baseURI = UriBuilder.fromUri("https://0.0.0.0:6969/manager").build();
            Gson gson = new Gson();
            WebTarget target = client.target(baseURI);

            AdminServerRequest adminRequest = new AdminServerRequest(3, 8060, false, false, false,1);
            adminRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(adminRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage(AdminKeyLoader.loadPrivateKey(), hashedMessage);
            adminRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            String json = gson.toJson(adminRequest);
            Response response = target.path("/up").request().header("nonce", adminRequest.getNonce())
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));

            int status = response.getStatus();
            System.out.println("Bring Up Server Status: " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
