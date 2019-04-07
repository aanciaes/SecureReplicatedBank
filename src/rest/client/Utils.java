package rest.client;

import javax.crypto.Cipher;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

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

    static public PublicKey getRelplicaPublicKey(int replicaId) {
        try {
        FileReader f = new FileReader("config/keys/publickey"+replicaId);

        BufferedReader r = new BufferedReader(f);
        String tmp = "";
        StringBuilder key = new StringBuilder();
        while ((tmp = r.readLine()) != null) {
            key.append(tmp);
        }
        f.close();
        r.close();

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key.toString()));
        return keyFactory.generatePublic(publicKeySpec);

        } catch (Exception e) {
            System.out.println("KEY NOT FOUND");
        }
        return null;
    }

    static public class InsecureHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


    static public byte[] generateHash(byte[] toHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(toHash);
            return digest.digest();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public byte[] decryptRequest(PublicKey pubk, byte[] data) {
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
