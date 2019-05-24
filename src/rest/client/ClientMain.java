package rest.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import java.util.Random;

/**
 * Main client. Runs all tests by calling the other clients
 */
public class ClientMain {

    private static List<KeyPair> users = new ArrayList();

    public static void main(String[] args) throws Exception {

        //Configuring standard log levels
        Configurator.setLevel(AddMoneyClient.class.getName(), Level.INFO);
        Configurator.setLevel(GetBalanceClient.class.getName(), Level.INFO);
        Configurator.setLevel(TransferClient.class.getName(), Level.INFO);
        Configurator.setLevel(Utils.class.getName(), Level.INFO);

        int faults = 1;

        CommandLine cmd = commandLineParser(args);

        if (cmd.hasOption("f")) {
            faults = Integer.parseInt(cmd.getOptionValue("f"));
        }

        if (cmd.hasOption("d")) {
            Configurator.setLevel(AddMoneyClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(GetBalanceClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(TransferClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(Utils.class.getName(), Level.DEBUG);
        }

        if (cmd.hasOption('t')) {
            Configurator.setLevel(AddMoneyClient.class.getName(), Level.OFF);
            Configurator.setLevel(GetBalanceClient.class.getName(), Level.OFF);
            Configurator.setLevel(TransferClient.class.getName(), Level.OFF);
            Configurator.setLevel(Utils.class.getName(), Level.OFF);
        }

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new Utils.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://0.0.0.0:8080/wallet/").build();
        WebTarget target = client.target(baseURI);

        int nUsers = 0;
        while (nUsers < 10) {
            try {
                KeyPair kp = Utils.generateNewKeyPair(1024);
                users.add(kp);
                AddMoneyWalletClient.addMoney(target, faults, AdminSgxKeyLoader.loadPrivateKey("adminPrivateKey"), kp.getPublic(), "1000.0");
                nUsers++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*GetBalanceTest test = new GetBalanceTest(target, faults);
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
        List<Long> aggregatedTransfer = new ArrayList<Long>(test.getTransferTimes());
        aggregatedTransfer.remove(0);

        thread2.join();
        test2.getBalanceTimes().remove(0);
        aggregatedBalance.addAll(test2.getBalanceTimes());
        test2.getTransferTimes().remove(0);
        aggregatedTransfer.addAll(test2.getTransferTimes());

        thread3.join();
        test3.getBalanceTimes().remove(0);
        aggregatedBalance.addAll(test3.getBalanceTimes());
        test3.getTransferTimes().remove(0);
        aggregatedTransfer.addAll(test3.getTransferTimes());

        Long accumulatedBalance = 0L;
        for (Long time : aggregatedBalance) {
            accumulatedBalance += time;
        }

        Long accumulatedTranfer = 0L;
        for (Long time : aggregatedTransfer) {
            accumulatedTranfer += time;
        }

        System.out.println("Get Balance Average: " + accumulatedBalance / aggregatedBalance.size() + "ms");
        System.out.println("Get Transfer Average: " + accumulatedBalance / aggregatedTransfer.size() + "ms");*/
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
            while (System.currentTimeMillis() - testTime < 1800) {
                int sender = rand.nextInt((users.size() - 1) + 1);
                Long timestampInit = System.currentTimeMillis();
                GetBalanceClient.getBalance(target, faults, users.get(sender), null);
                getBalanceTimes.add(System.currentTimeMillis() - timestampInit);

                sender = rand.nextInt((users.size() - 1) + 1);
                timestampInit = System.currentTimeMillis();
                GetBalanceClient.getBalance(target, faults, users.get(sender), null);
                getTransferTimes.add(System.currentTimeMillis() - timestampInit);

                double amount = 1000 * rand.nextDouble();
                sender = rand.nextInt((users.size() - 1) + 1);
                int receiver = rand.nextInt((users.size() - 1) + 1);
                timestampInit = System.currentTimeMillis();
                TransferClient.transfer(target, faults, users.get(sender), Base64.getEncoder().encodeToString(users.get(receiver).getPublic().getEncoded()), amount);
                getTransferTimes.add(System.currentTimeMillis() - timestampInit);

                amount = 1000 * rand.nextDouble();
                sender = rand.nextInt((users.size() - 1) + 1);
                receiver = rand.nextInt((users.size() - 1) + 1);
                timestampInit = System.currentTimeMillis();
                TransferClient.transfer(target, faults, users.get(sender), Base64.getEncoder().encodeToString(users.get(receiver).getPublic().getEncoded()), amount);
                getBalanceTimes.add(System.currentTimeMillis() - timestampInit);

                timestampInit = System.currentTimeMillis();
                GetBalanceClient.getBalance(target, faults, users.get(0), null);
                getBalanceTimes.add(System.currentTimeMillis() - timestampInit);

                amount = 1000 * rand.nextDouble();
                sender = rand.nextInt((users.size() - 1) + 1);
                receiver = rand.nextInt((users.size() - 1) + 1);
                timestampInit = System.currentTimeMillis();
                TransferClient.transfer(target, faults, users.get(sender), Base64.getEncoder().encodeToString(users.get(receiver).getPublic().getEncoded()), amount);
                getBalanceTimes.add(System.currentTimeMillis() - timestampInit);
            }
        }
    }
}