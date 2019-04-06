package rest.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class ClientMain {

    public static void main(String[] args) {

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/").build();
        WebTarget target = client.target(baseURI);
        int nUsers = 10;
        while(nUsers > 0){
            AddMoneyClient.addMoney(target, null, 200.0);
            nUsers--;
        }







    }
}
