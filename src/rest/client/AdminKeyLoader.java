package rest.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Class that loads the specific keys for the admin user.
 * This keys can be changed on runtime since it is always reading from the file. No cache is made
 */
public class AdminKeyLoader {

    public static PublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = readFromFile("adminKeys/adminPublicKey");

        return getPublicKeyFromString(key);
    }

    public static PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = readFromFile("adminKeys/adminPrivateKey");

        return getPrivateKeyFromString(key);
    }

    /**
     * Reads from file
     *
     * @param filename Filename to read from
     * @return File as a string
     * @throws IOException If filename does not exist
     */
    private static String readFromFile(String filename) throws IOException {
        FileReader f = new FileReader(filename);

        BufferedReader r = new BufferedReader(f);
        String tmp = "";
        StringBuilder key = new StringBuilder();
        while ((tmp = r.readLine()) != null) {
            key.append(tmp);
        }
        f.close();
        r.close();

        return key.toString();
    }

    //utility methods for going from string to public/private key
    private static PrivateKey getPrivateKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
        return keyFactory.generatePrivate(privateKeySpec);
    }

    private static PublicKey getPublicKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
        return keyFactory.generatePublic(publicKeySpec);
    }
    //
}
