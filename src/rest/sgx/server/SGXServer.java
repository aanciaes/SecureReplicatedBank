package rest.sgx.server;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class SGXServer {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("javax.net.ssl.keyStore", "sgxServer.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "qwerty");
    }
    public static final int PORT = 6699;

    public static void main(String[] args) throws Exception {

        URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(PORT).build();

        ResourceConfig config = new ResourceConfig();
        config.register(new SGXServerResources());

        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

        System.err.println("SSL SGX Server ready @ " + baseUri);
    }

}
