package rest.client;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

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
        Configurator.setLevel(AddMoneyClient.class.getName(), Level.INFO);
        Configurator.setLevel(GetBalanceClient.class.getName(), Level.INFO);
        Configurator.setLevel(TransferClient.class.getName(), Level.INFO);
        Configurator.setLevel(Utils.class.getName(), Level.INFO);

        if (args.length == 1) {
            if (args[0].equals("-d")){
                Configurator.setLevel(AddMoneyClient.class.getName(), Level.DEBUG);
                Configurator.setLevel(GetBalanceClient.class.getName(), Level.INFO);
                Configurator.setLevel(TransferClient.class.getName(), Level.INFO);
                Configurator.setLevel(Utils.class.getName(), Level.DEBUG);
            }
        }

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/wallet/").build();
        WebTarget target = client.target(baseURI);
        int nUsers = 0;
        while (nUsers < 2) {
            try {
                KeyPair kp = Utils.generateNewKeyPair(1024);
                users.add(kp);
                AddMoneyClient.addMoney(target, AdminKeyLoader.loadPrivateKey(), kp.getPublic(), 1000.0);
                nUsers++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        GetBalanceClient.getBalance(target, users.get(0));

        TransferClient.transfer(target, users.get(0), Base64.getEncoder().encodeToString(users.get(1).getPublic().getEncoded()), 100.0);
        TransferClient.transfer(target, users.get(0), Base64.getEncoder().encodeToString(users.get(1).getPublic().getEncoded()), 100.0);

        GetBalanceClient.getBalance(target, users.get(0));
    }
}