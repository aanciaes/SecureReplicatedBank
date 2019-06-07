package rest.client;

import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import java.math.BigInteger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import rest.client.conditional.ConditionalClient;
import rest.client.create.CreateHomoAddClient;
import rest.client.create.CreateHomoOpeIntClient;
import rest.client.create.CreateWalletClient;
import rest.client.get.GetBalanceClient;
import rest.client.get.GetBetweenClient;
import rest.client.set.SetBalanceClient;
import rest.client.sum.SumClient;
import rest.client.sum.TransferClient;
import rest.server.model.DataType;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Update;
import rest.utils.Utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * Main client. Runs all tests by calling the other clients
 */
public class ClientMain {

    private static List<KeyPair> users = new ArrayList();
    private static List<PaillierKey> paillierKeys = new ArrayList<>();
    private static Long value = 0l;

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.trustStore", "client.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "qwerty");

        //Configuring standard log levels
        Configurator.setLevel(TestClient.class.getName(), Level.INFO);
        Configurator.setLevel(GetBalanceClient.class.getName(), Level.INFO);
        Configurator.setLevel(TransferClient.class.getName(), Level.INFO);
        Configurator.setLevel(Utils.class.getName(), Level.INFO);
        Configurator.setLevel(CreateWalletClient.class.getName(), Level.INFO);
        Configurator.setLevel(CreateHomoOpeIntClient.class.getName(), Level.INFO);
        Configurator.setLevel(CreateHomoAddClient.class.getName(), Level.INFO);
        Configurator.setLevel(GetBetweenClient.class.getName(), Level.INFO);
        Configurator.setLevel(SetBalanceClient.class.getName(), Level.INFO);
        Configurator.setLevel(SumClient.class.getName(), Level.INFO);
        Configurator.setLevel(ConditionalClient.class.getName(), Level.INFO);

        int faults = 1;

        CommandLine cmd = commandLineParser(args);

        if (cmd.hasOption("f")) {
            faults = Integer.parseInt(cmd.getOptionValue("f"));
        }

        if (cmd.hasOption("d")) {
            Configurator.setLevel(TestClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(GetBalanceClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(TransferClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(Utils.class.getName(), Level.DEBUG);
            Configurator.setLevel(CreateWalletClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(CreateHomoOpeIntClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(CreateHomoAddClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(GetBetweenClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(SetBalanceClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(SumClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(ConditionalClient.class.getName(), Level.DEBUG);
        }

        if (cmd.hasOption('t')) {
            Configurator.setLevel(TestClient.class.getName(), Level.OFF);
            Configurator.setLevel(GetBalanceClient.class.getName(), Level.OFF);
            Configurator.setLevel(TransferClient.class.getName(), Level.OFF);
            Configurator.setLevel(Utils.class.getName(), Level.OFF);
            Configurator.setLevel(CreateWalletClient.class.getName(), Level.OFF);
            Configurator.setLevel(CreateHomoOpeIntClient.class.getName(), Level.OFF);
            Configurator.setLevel(CreateHomoAddClient.class.getName(), Level.OFF);
            Configurator.setLevel(GetBetweenClient.class.getName(), Level.OFF);
            Configurator.setLevel(SetBalanceClient.class.getName(), Level.OFF);
            Configurator.setLevel(SumClient.class.getName(), Level.OFF);
            Configurator.setLevel(ConditionalClient.class.getName(), Level.OFF);
        }

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/wallet/").build();
        WebTarget target = client.target(baseURI);

        int nUsers = 0;
        while (nUsers < 3) {
            try {
                KeyPair kp = Utils.generateNewKeyPair(1024);
                users.add(nUsers, kp);

                switch (nUsers) {
                    case 0:
                        CreateWalletClient.addMoney(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000.0");
                        break;
                    case 1:
                        PaillierKey pk = HomoAdd.generateKey();
                        paillierKeys.add(0, pk);

                        CreateHomoAddClient.createAccount(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000", pk);
                        break;
                    case 2:
                        CreateHomoOpeIntClient.createAccount(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000", "homo");
                        break;
                        default:break;
                }
                //CreateWalletClient.addMoney(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000.0");
                //CreateWalletClient.addMoney(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000");

                //CreateHomoAddClient.createAccount(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000", pk);

                //CreateHomoOpeIntClient.createAccount(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000", "homo");
                nUsers++;
            } catch (Exception e) {
                e.printStackTrace();
            }

            HomoOpeInt opeInt = new HomoOpeInt("homo");
            value = opeInt.encrypt(10);
        }

        GetBalanceTest test = new GetBalanceTest(target, faults);
        GetBalanceTest test2 = new GetBalanceTest(target, faults);
        GetBalanceTest test3 = new GetBalanceTest(target, faults);
        Thread thread1 = new Thread(test);
        Thread thread2 = new Thread(test2);
        Thread thread3 = new Thread(test3);

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        List<Long> aggregatedBalance = new ArrayList<Long>(test.getBalanceTimes());
        aggregatedBalance.remove(0);

        thread2.join();
        test2.getBalanceTimes().remove(0);
        aggregatedBalance.addAll(test2.getBalanceTimes());

        thread3.join();
        test3.getBalanceTimes().remove(0);
        aggregatedBalance.addAll(test3.getBalanceTimes());

        Long accumulatedBalance = 0L;
        for (Long time : aggregatedBalance) {
            accumulatedBalance += time;
        }

        System.out.println("Get Wallet aet Average: " + accumulatedBalance / aggregatedBalance.size() + "ms");
    }

    private static CommandLine commandLineParser(String[] args) throws ParseException {
        // create Options object
        Options options = new Options();
        options.addOption("d", "debug", false, "debug mode");
        options.addOption("t", "tests", false, "test mode, no client logs");
        options.addOption("f", "faults", true, "number of faults to tolerate mode");

        CommandLineParser parser = new DefaultParser();

        return parser.parse(options, args);
    }

    static class GetBalanceTest implements Runnable {
        int faults;
        WebTarget target;
        List<Long> getBalanceTimes = new ArrayList<Long>();
        List<Long> getTransferTimes = new ArrayList<Long>();


        public GetBalanceTest(WebTarget target, int faults) {
            this.faults = faults;
            this.target = target;
        }

        public List<Long> getBalanceTimes() {
            return getBalanceTimes;
        }

        public List<Long> getTransferTimes() {
            return getTransferTimes;
        }

        @SuppressWarnings("Duplicates")
        @Override
        public void run() {
            Long testTime = System.currentTimeMillis();
            Random rand = new Random();
            String opeIntKey = "anotherkey";

            while (System.currentTimeMillis() - testTime < 90000) {
                try {
                    int sender = rand.nextInt((users.size() - 1) + 1);
                    int lower = rand.nextInt((1000 - 1) + 1);
                    int higher = rand.nextInt((2000 - lower) + lower);

                    List<Update> updates = new ArrayList<>();
                    updates.add(new Update(0, Base64.getEncoder().encodeToString(users.get(0).getPublic().getEncoded()), "10", null));
                    updates.add(new Update(1, Base64.getEncoder().encodeToString(users.get(0).getPublic().getEncoded()), "10", null));

                    updates.add(new Update(0, Base64.getEncoder().encodeToString(users.get(1).getPublic().getEncoded()), HomoAdd.encrypt(new BigInteger("10"), paillierKeys.get(0)).toString(), paillierKeys.get(0).getNsquare().toString()));
                    updates.add(new Update(1, Base64.getEncoder().encodeToString(users.get(1).getPublic().getEncoded()), HomoAdd.encrypt(new BigInteger("10"), paillierKeys.get(0)).toString(), paillierKeys.get(0).getNsquare().toString()));

                    updates.add(new Update(0, Base64.getEncoder().encodeToString(users.get(2).getPublic().getEncoded()), value.toString(), null));
                    updates.add(new Update(1, Base64.getEncoder().encodeToString(users.get(2).getPublic().getEncoded()), value.toString(), null));


                    Long timestampInit = System.currentTimeMillis();

                    //SetBalanceClient.setBalance(target, faults, users.get(sender), "homo", "10", DataType.HOMO_OPE_INT );

                    //SumClient.sumMoney(target, faults, users.get(sender), DataType.HOMO_OPE_INT, "1000", "homoopeintkey");

                    //GetBetweenClient.getBalanceBetween(target, faults, "homo", DataType.HOMO_OPE_INT, lower, higher, null);
                    //GetBalanceClient.getBalance(target, faults, users.get(sender), "homo");

                    ConditionalClient.conditional_upd(target, faults, Base64.getEncoder().encodeToString(users.get(sender).getPublic().getEncoded()), 0.0, updates, 2);

                    getBalanceTimes.add(System.currentTimeMillis() - timestampInit);
                }catch (Exception e){
                    //
                }
            }
        }
    }
}