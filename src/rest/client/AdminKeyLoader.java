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

public class AdminKeyLoader {

    public static PublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = readFromFile("adminKeys/adminPublicKey");

        return getPublicKeyFromString(key);
    }

    public static PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = readFromFile("adminKeys/adminPrivateKey");

        return getPrivateKeyFromString(key);
    }

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
}
