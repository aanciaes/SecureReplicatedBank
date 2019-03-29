package rest.server;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class RendezVousServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);

        URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

        ResourceConfig config = new ResourceConfig();
        config.register(new RendezVousResources(port));

        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

        System.err.println("SSL REST Bank Server ready @ " + baseUri);
    }
}
