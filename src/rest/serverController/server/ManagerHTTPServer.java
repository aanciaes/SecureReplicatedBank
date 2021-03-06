package rest.serverController.server;

import java.net.URI;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class ManagerHTTPServer {

    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "server.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "qwerty");
        int port = 6969;
        URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

        ResourceConfig config = new ResourceConfig();
        config.register(new ManagerHTTPServerResources());
        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

        System.err.println("SSL REST Admin Bank Server ready @ " + baseUri);
    }
}