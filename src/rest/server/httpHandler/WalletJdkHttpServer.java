package rest.server.httpHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import rest.server.replica.ReplicaServer;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class WalletJdkHttpServer {

    public static void main(String[] args) throws Exception {
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
            System.err.println("Usage: WalletJdkHttpServer -p <port> -id <replica id>");
            System.exit(-1);
        }

        //replica id
        if (cmd.hasOption("id")) {
            replicaId = Integer.parseInt(cmd.getOptionValue("id"));
        } else {
            System.err.println("Usage: WalletJdkHttpServer -p <port> -id <replica id>");
            System.exit(-1);
        }

        //Set debug level
        if (cmd.hasOption('d')) {
            Configurator.setLevel(WalletServerResources.class.getName(), Level.DEBUG);
            Configurator.setLevel(ReplicaServer.class.getName(), Level.DEBUG);
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
        // create Options object
        Options options = new Options();
        options.addOption("p", "port", true, "port");
        options.addOption("id", "replicaId", true, "replica id");
        options.addOption("d", "debug", false, "debug mode");
        options.addOption("u", "unpredictable", false, "unpredictable mode");

        CommandLineParser parser = new DefaultParser();

        return parser.parse(options, args);
    }
}