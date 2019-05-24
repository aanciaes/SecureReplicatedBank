package rest.sgx;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import rest.server.httpHandler.WalletServerResources;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import java.net.InetAddress;
import java.net.URI;

public class SGXServer {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
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
