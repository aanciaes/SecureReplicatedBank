package rest.server.httpHandler;

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
        if (args.length < 2) {
            System.err.println("Usage: WalletJdkHttpServer <port> <replica id>");
            System.exit(-1);
        }

        Configurator.setLevel(WalletServerResources.class.getName(), Level.INFO);
        Configurator.setLevel(ReplicaServer.class.getName(), Level.INFO);

        int port = Integer.parseInt(args[0]);
        int replicaId = Integer.parseInt(args[1]);

        if (args.length == 3){
            if (args[2].equals("-d")){
                Configurator.setLevel(WalletServerResources.class.getName(), Level.DEBUG);
                Configurator.setLevel(ReplicaServer.class.getName(), Level.DEBUG);
            }
        }

        URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

        ResourceConfig config = new ResourceConfig();
        config.register(new WalletServerResources(port, replicaId));

        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

        System.err.println("SSL REST Bank Server ready @ " + baseUri);
    }
}