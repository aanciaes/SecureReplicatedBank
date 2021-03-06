package rest.serverController.client;

import com.google.gson.Gson;
import java.net.URI;
import java.util.Base64;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import rest.serverController.model.AdminServerRequest;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Utils;

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

            AdminServerRequest adminRequest = new AdminServerRequest(0, 8080, false, false, false, 1);
            adminRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(adminRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage("RSA", "SunJCE", AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), hashedMessage);
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
