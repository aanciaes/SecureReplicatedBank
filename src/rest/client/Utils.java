package rest.client;

import javax.crypto.Cipher;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class Utils {

    public static long generateNonce() {
        // create instance of SecureRandom class
        SecureRandom rand = new SecureRandom();

        return rand.nextLong();
    }

    public static KeyPair generateNewKeyPair (int size) {

        try {
            KeyPairGenerator kpgen = KeyPairGenerator.getInstance("RSA");
            kpgen.initialize(size);

            return kpgen.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] hashMessage (byte[] messageBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(messageBytes);
            return digest.digest();
        } catch (Exception e){
            return new byte[0];
        }
    }

    public static byte[] encryptMessage (Key key, byte[] message) {
        try{
            Cipher c = Cipher.getInstance("RSA", "SunJCE");
            c.init(Cipher.ENCRYPT_MODE, key);
            return c.doFinal(message);
        }catch (Exception e) {
            return new byte[0];
        }
    }

    static public class InsecureHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}
