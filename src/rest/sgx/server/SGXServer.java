package rest.sgx.server;

import java.net.URI;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class SGXServer {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("javax.net.ssl.keyStore", "sgxServer.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "qwerty");
    }

    private static final int PORT = 6699;

    public static void main(String[] args) throws Exception {

        URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(PORT).build();

        ResourceConfig config = new ResourceConfig();
        config.register(new SGXServerResources());

        //Set default log levels
        Configurator.setLevel(SGXServerResources.class.getName(), Level.INFO);

        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

        System.err.println("SSL SGX Server ready @ " + baseUri);
    }
}
