package rest.client.create;

import hlib.hj.mlib.HomoOpeInt;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.PaillierKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.client.ConditionalClient;
import rest.client.get.GetBalanceClient;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Update;
import rest.utils.Utils;

/**
 * Client that adds money
 */
public class CreateClient {

    private static Logger logger = LogManager.getLogger(CreateClient.class.getName());

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
            PaillierKey pk = HomoAdd.generateKey();
            String opeIntKey = "anotherkey";

            //CreateWalletClient.addMoney(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000");
            //CreateHomoAddClient.createAccount(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000", pk);
            CreateHomoOpeIntClient.createAccount(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000", opeIntKey);

            //SetBalanceClient.setBalance(target, faults, kp, opeIntKey, "12", DataType.HOMO_OPE_INT);

            //GetBetweenClient.getBalanceBetween(target, faults, opeIntKey, DataType.HOMO_ADD, 980, 4000, null);
            //GetBetweenClient.getBalanceBetween(target, faults, opeIntKey, DataType.HOMO_OPE_INT, 980, 1200, "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQK");

            //SumClient.sumMoney(target, faults, kp, DataType.WALLET, "1000", null);
            //SumClient.sumMoney(target, faults, kp, DataType.HOMO_OPE_INT, "1000", opeIntKey);
            //SumClient.sumMoney(target, faults, kp, DataType.HOMO_OPE_INT, "1000", opeIntKey);
            //SumClient.sumMoney(target, faults, kp, DataType.HOMO_OPE_INT, "1000", opeIntKey);
            //SumClient.sumMoney(target, faults, kp, DataType.HOMO_ADD, "1000", HelpSerial.toString(pk));

            //GetBalanceClient.getBalance(target, faults, kp,  null);
            //GetBalanceClient.getBalance(target, faults, kp, HelpSerial.toString(pk));
            //SetBalanceClient.setBalance(target, faults, kp, HelpSerial.toString(pk), "4000", DataType.HOMO_ADD);
            //GetBalanceClient.getBalance(target, faults, kp, opeIntKey);

            List<Update> updates = new ArrayList<>();

            //updates.add(new Update(0, Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()), "400", null));
            //updates.add(new Update(1, Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()), "400", null));

            //updates.add(new Update(0, Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()), HomoAdd.encrypt(new BigInteger("10"), pk).toString(), pk.getNsquare().toString()));
            //updates.add(new Update(1, Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()), HomoAdd.encrypt(new BigInteger("10"), pk).toString(), pk.getNsquare().toString()));

            updates.add(new Update(0, Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()), String.valueOf(new HomoOpeInt(opeIntKey).encrypt(20)), null));
            //updates.add(new Update(1, Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()), String.valueOf(new HomoOpeInt(opeIntKey).encrypt(20)), null));

            //0 -> db[cond_key] = cond_value
            //1 -> db[cond_key] != cond_value
            // 2 -> db[cond_key] > cond_value
            // 3 -> db[cond_key] >= cond_value
            // 4 -> db[cond_key] < cond_value
            // 5 -> db[cond_key] <= cond_value
            // ir buscar todos os clientes que holdem a condicao passada como variavel e aplicar a lista de updates
            ConditionalClient.conditional_upd(target, faults, Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()), 1000.0, updates, 0);

            //GetBalanceClient.getBalance(target, faults, kp, null);
            //GetBalanceClient.getBalance(target, faults, kp, HelpSerial.toString(pk));
            GetBalanceClient.getBalance(target, faults, kp, opeIntKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}