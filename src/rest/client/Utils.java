package rest.client;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.util.KeyLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientResponse;
import rest.server.model.ReplicaResponse;

import javax.crypto.Cipher;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

public class Utils {

    private static Logger logger = LogManager.getLogger(Utils.class.getName());


    public static long generateNonce() {
        // create instance of SecureRandom class
        SecureRandom rand = new SecureRandom();

        return rand.nextLong();
    }

    public static KeyPair generateNewKeyPair(int size) {

        try {
            KeyPairGenerator kpgen = KeyPairGenerator.getInstance("RSA");
            kpgen.initialize(size);

            return kpgen.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] hashMessage(byte[] messageBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(messageBytes);
            return digest.digest();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static byte[] encryptMessage(Key key, byte[] message) {
        try {
            Cipher c = Cipher.getInstance("RSA", "SunJCE");
            c.init(Cipher.ENCRYPT_MODE, key);
            return c.doFinal(message);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static int verifyReplicaResponse(long nonce, ClientResponse clientResponse) {
        int conflicts = 0;

        for (ReplicaResponse replicaResponse : clientResponse.getResponses()) {
            logger.debug (String.format("ReplicaId: %d, Status: %d, body: %s", replicaResponse.getReplicaId(), replicaResponse.getStatusCode(), replicaResponse.getBody().toString()));
            if (nonce + 1 != replicaResponse.getNonce()) {
                conflicts++;
                logger.warn("NONCE CONFLICT");
            } else if (!clientResponse.getBody().equals(replicaResponse.getBody())) {
                conflicts++;
                logger.warn("AMOUNT CONFLICT");
            } else if (replicaResponse.getStatusCode() != 200) {
                conflicts++;
                logger.warn("STATUS CONFLICT");
            } else {
                if (!Utils.verifyReplicaResponseSignature(
                        replicaResponse.getReplicaId(),
                        Base64.getDecoder().decode(replicaResponse.getSerializedMessage()),
                        Base64.getDecoder().decode(replicaResponse.getSignature()))
                ) {
                    logger.warn("SIGNATURE CONFLICT");
                    conflicts++;
                }
            }
        }
        return conflicts;
    }

    private static boolean verifyReplicaResponseSignature(int replicaId, byte[] serializedMessage, byte[] signature) {
        try {
            KeyLoader keyLoader = new RSAKeyLoader(replicaId, "config", false, "SHA512withRSA");
            PublicKey pk = keyLoader.loadPublicKey(replicaId);

            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);
            sig.update(serializedMessage);

            return sig.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static public class InsecureHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
