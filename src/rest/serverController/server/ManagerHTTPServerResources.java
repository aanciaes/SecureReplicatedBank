package rest.serverController.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.ws.rs.core.HttpHeaders;
import rest.serverController.model.AdminServerRequest;
import rest.utils.AdminSgxKeyLoader;

/**
 * Restful resources of admin server
 */
public class ManagerHTTPServerResources implements ManagerServer {
    private Map<Integer, Process> processes = new HashMap<Integer, Process>();

    @Override
    public void upServer(HttpHeaders headers, AdminServerRequest adminServerRequest) {
        if (!verifyIntegrity(adminServerRequest)) {
            return;
        }
        try {
            //java -cp projectJar/lab1.jar rest.server.httpHandler.WalletJdkHttpServer -id 0 -p 8080

            File file = new File("projectJar/project-v2.2.jar");
            String path = file.getPath();
            /*URL[] classLoaderUrls = new URL[]{new URL("file://" + path)};
            // Create a new URLClassLoader
            URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);
            final Class<?> clazz = Class.forName("rest.server.httpHandler.WalletJdkHttpServer");
            final Method method = clazz.getMethod("main", String[].class);
*/
            final Object[] arg = new Object[4];
            arg[0] = "-p";
            arg[1] = adminServerRequest.getServerPort();
            arg[2] = "-id";
            arg[3] = adminServerRequest.getServerId();
            if (adminServerRequest.isDebug()) {
                arg[4] = "-d";
            }
            if (adminServerRequest.isTestMode()) {
                arg[5] = "-t";
            }
            if (adminServerRequest.isUnpredictable()) {
                arg[6] = "-u";
            }
            //method.invoke(null, arg);
            //Process -> get process
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", "projectJar/project-v2.2.jar", "rest.server.httpHandler.WalletJdkHttpServer", "-id", "" + adminServerRequest.getServerId(), "-p", "" + adminServerRequest.getServerPort());
            Process p = pb.start();
            processes.put(adminServerRequest.getServerId(), p);

            new Thread(new ProcessIOErrorAttacher(adminServerRequest.getServerId(), p)).start();
            new Thread(new ProcessIOStdoutAttacher(adminServerRequest.getServerId(), p)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downServer(HttpHeaders headers, AdminServerRequest adminServerRequest) {
        if (!verifyIntegrity(adminServerRequest)) {
            return;
        }

        Process p = processes.get(adminServerRequest.getServerId());
        p.destroy();
    }

    private boolean verifyIntegrity(AdminServerRequest adminServerRequest) {
        try {
            byte[] hashMessage = generateHash(adminServerRequest.getSerializeMessage().getBytes());
            byte[] decryptedHash = decryptRequest(AdminSgxKeyLoader.loadPublicKey("adminPublicKey"), Base64.getDecoder().decode(adminServerRequest.getSignature()));

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

    public class ProcessIOErrorAttacher implements Runnable {

        private int id;
        private Process process;

        ProcessIOErrorAttacher (int id, Process process) {
            this.id = id;
            this.process = process;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine = null;
                while ( (errorLine = reader.readLine()) != null) {
                    System.out.println("Process id: " + id + ": " + errorLine);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ProcessIOStdoutAttacher implements Runnable {

        private int id;
        private Process process;

        ProcessIOStdoutAttacher (int id, Process process) {
            this.id = id;
            this.process = process;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String errorLine = null;
                while ( (errorLine = reader.readLine()) != null) {
                    System.out.println("Process id: " + id + ": " + errorLine);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
