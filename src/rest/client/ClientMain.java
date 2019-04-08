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

        if (cmd.hasOption("f")){
            faults = Integer.parseInt(cmd.getOptionValue("f"));
        }


        if (cmd.hasOption("d")) {
            Configurator.setLevel(AddMoneyClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(GetBalanceClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(TransferClient.class.getName(), Level.DEBUG);
            Configurator.setLevel(Utils.class.getName(), Level.DEBUG);
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
                AddMoneyClient.addMoney(target, faults,  AdminKeyLoader.loadPrivateKey(), kp.getPublic(), 1000.0);
                nUsers++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        GetBalanceClient.getBalance(target, faults, users.get(0));

        TransferClient.transfer(target, faults, users.get(0), Base64.getEncoder().encodeToString(users.get(1).getPublic().getEncoded()), 100.0);
        TransferClient.transfer(target, faults, users.get(0), Base64.getEncoder().encodeToString(users.get(1).getPublic().getEncoded()), 100.0);

        GetBalanceClient.getBalance(target, faults, users.get(0));
    }

    private static CommandLine commandLineParser(String[] args) throws ParseException {
        // create Options object
        Options options = new Options();
        options.addOption("d", "debug", false, "debug mode");
        options.addOption("f", "faults", false, "number of faults to tolerate mode");

        CommandLineParser parser = new DefaultParser();

        return parser.parse(options, args);
    }
}