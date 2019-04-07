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


/*try {
            KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
            PublicKey pk = keyLoader.loadPublicKey(tomMessages[0].getSender());
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);

            sig.update(tomMessages[0].serializedMessage);
            System.out.println(sig.verify(tomMessages[0].serializedMessageSignature));

            PublicKey pk1 = keyLoader.loadPublicKey(tomMessages[1].getSender());
            Signature sig1 = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig1.initVerify(pk1);

            sig1.update(tomMessages[1].serializedMessage);
            System.out.println(sig1.verify(tomMessages[1].serializedMessageSignature));

        } catch (Exception e) {
            e.printStackTrace();
        }*/