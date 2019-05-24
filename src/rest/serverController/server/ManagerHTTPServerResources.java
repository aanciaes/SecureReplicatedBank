package rest.serverController.server;

import rest.client.AdminKeyLoader;
import rest.server.model.*;
import rest.serverController.model.AdminServerRequest;

import javax.crypto.Cipher;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Restful resources of admin server
 */
public class ManagerHTTPServerResources implements ManagerServer {
    private Map<Integer, Process> processes = new HashMap<Integer, Process>();
    @Override
    public void upServer(HttpHeaders headers, AdminServerRequest adminServerRequest) {
        //TODO:
        if(!verifyIntegrity(adminServerRequest)){
            return;
        }
        try {
            File file = new File("out/artifacts/SecureReplicatedBank_jar/SecureReplicatedBank.jar");
            String path = file.getPath();
            URL[] classLoaderUrls = new URL[]{new URL("file://" + path)};
            // Create a new URLClassLoader
            URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);
            final Class<?> clazz = Class.forName("rest.server.httpHandler.WalletJdkHttpServer");
            final Method method = clazz.getMethod("main", String[].class);

            final Object[] arg = new Object[1];
            arg[0] = "-p " + adminServerRequest.getServerPort() + " -id "+ adminServerRequest.getServerId();
            if(adminServerRequest.isDebug()){
               arg[0] += " -d";
            }
            if(adminServerRequest.isTestMode()){
                arg[0] += " -t";
            }
            if(adminServerRequest.isUnpredictable()){
                arg[0] += " -u";
            }
            method.invoke(null, arg);
            //Process -> get process
            ProcessBuilder pb = new ProcessBuilder("java", "-jar",  "file://" + path);
            Process p = pb.start();
            processes.put(adminServerRequest.getServerId(), p);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downServer(HttpHeaders headers, AdminServerRequest adminServerRequest) {
        //TODO:
        if(!verifyIntegrity(adminServerRequest)){
            return;
        }
        if(processes.size() < adminServerRequest.getFaults()){
            return;
        }

        Process p = processes.get(adminServerRequest.getServerId());
        p.destroy();
    }

    private boolean verifyIntegrity(AdminServerRequest adminServerRequest) {
        try {
            byte[] hashMessage = generateHash(adminServerRequest.getSerializeMessage().getBytes());
            byte[] decryptedHash = decryptRequest(AdminKeyLoader.loadPublicKey(), Base64.getDecoder().decode(adminServerRequest.getSignature()));

            // Could not decrypt hash from message
            if (decryptedHash == null) {
                //throw new WebApplicationException(Response.Status.FORBIDDEN);
                return false;
            }
            // Comparing hashes. If not equal, message has been tampered with
            if (!Arrays.equals(hashMessage, decryptedHash)) {
                //throw new WebApplicationException(Response.Status.FORBIDDEN);
                return false;
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Generates an hash for a message with SHA-512 algorithm
     *
     * @param toHash Message to hash
     * @return Hashed message
     */
    private byte[] generateHash(byte[] toHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(toHash);
            return digest.digest();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypts a message with a given key
     *
     * @param pubk The key to decrypt the message
     * @param data The data to be decrypted
     * @return data in plain text
     */
    private byte[] decryptRequest(PublicKey pubk, byte[] data) {
        try {
            Cipher c = Cipher.getInstance("RSA", "SunJCE");
            c.init(Cipher.DECRYPT_MODE, pubk);
            return c.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
