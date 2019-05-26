package rest.server.httpHandler;

import java.net.URI;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import rest.server.replica.ReplicaServer;

public class WalletJdkHttpServer {

    private static Options cmdOptions = new Options();
    ;

    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "server.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "qwerty");
        System.setProperty("javax.net.ssl.trustStore", "sgxClient.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "qwerty");

        int port = 8080;
        int replicaId = 0;
        boolean unpredictable = false;

        //Set default log levels
        Configurator.setLevel(WalletServerResources.class.getName(), Level.INFO);
        Configurator.setLevel(ReplicaServer.class.getName(), Level.INFO);

        CommandLine cmd = commandLineParser(args);

        //port
        if (cmd.hasOption('p')) {
            port = Integer.parseInt(cmd.getOptionValue('p'));
        } else {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("WalletJdkHttpServer -p <port> -id <replicaId> [OPTIONS]", cmdOptions);
            System.exit(-1);
        }

        //replica id
        if (cmd.hasOption("id")) {
            replicaId = Integer.parseInt(cmd.getOptionValue("id"));
        } else {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("WalletJdkHttpServer -p <port> -id <replicaId> [OPTIONS]", cmdOptions);
            System.exit(-1);
        }

        //Set debug level
        if (cmd.hasOption('d')) {
            Configurator.setLevel(WalletServerResources.class.getName(), Level.DEBUG);
            Configurator.setLevel(ReplicaServer.class.getName(), Level.DEBUG);
        }

        //Set debug level
        if (cmd.hasOption('t')) {
            Configurator.setLevel(WalletServerResources.class.getName(), Level.OFF);
            Configurator.setLevel(ReplicaServer.class.getName(), Level.OFF);
        }

        //Set server unpredictable mode
        if (cmd.hasOption('u')) {
            unpredictable = true;
        }

        URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

        ResourceConfig config = new ResourceConfig();
        config.register(new WalletServerResources(replicaId, unpredictable));

        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

        System.err.println("SSL REST Bank Server ready @ " + baseUri);
    }

    private static CommandLine commandLineParser(String[] args) throws ParseException {
        cmdOptions.addRequiredOption("p", "port", true, "port");
        cmdOptions.addRequiredOption("id", "replicaId", true, "replica id");
        cmdOptions.addOption("d", "debug", false, "debug mode");
        cmdOptions.addOption("t", "tests", false, "test mode, no prints");
        cmdOptions.addOption("u", "unpredictable", false, "unpredictable mode");

        CommandLineParser parser = new DefaultParser();

        try {
            return parser.parse(cmdOptions, args);
        } catch (MissingOptionException missingOptionException) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("WalletJdkHttpServer -p <port> -id <replicaId> [OPTIONS]", cmdOptions);
            System.exit(-1);
            return null;
        }
    }
}