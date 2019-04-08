package rest.client;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.util.KeyLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.ClientResponse;
import rest.server.model.ReplicaResponse;
import rest.server.model.WalletOperationType;

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

    /**
     * Generates a long random nonce
     *
     * @return Long random nonce
     */
    public static long generateNonce() {
        // create instance of SecureRandom class
        SecureRandom rand = new SecureRandom();

        return rand.nextLong();
    }

    /**
     * Generates a new KeyPair
     *
     * @param size size of the private key
     * @return A new KeyPair, containing a public and a private key with given size
     */
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

    /**
     * Hash a specific message with SHA-512 algorithm
     *
     * @param messageBytes data to hash
     * @return The hash of data
     */
    public static byte[] hashMessage(byte[] messageBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(messageBytes);
            return digest.digest();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * Encrypts a some data with a key
     *
     * @param key     Key to encrypt message
     * @param message data to encrypt
     * @return Encrypted data
     */
    public static byte[] encryptMessage(Key key, byte[] message) {
        try {
            Cipher c = Cipher.getInstance("RSA", "SunJCE");
            c.init(Cipher.ENCRYPT_MODE, key);
            return c.doFinal(message);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * Verifies a replica response. It verifies ot signature, the nonce to avoid replaying, its operation, its amount and its status
     *
     * @param nonce          Nonce to verify against
     * @param clientResponse Client Response containing all replica responses to be verified
     * @param operationType  Operation type to verify against
     * @return The number of conflicts, or number of verification errors on all client client requests
     */
    public static int verifyReplicaResponse(long nonce, ClientResponse clientResponse, WalletOperationType operationType) {
        int conflicts = 0;

        for (ReplicaResponse replicaResponse : clientResponse.getResponses()) {
            logger.debug(String.format("ReplicaId: %d, Status: %d, Operation Type: %s, body: %s", replicaResponse.getReplicaId(), replicaResponse.getStatusCode(), replicaResponse.getOperationType().toString(), replicaResponse.getBody().toString()));
            if (nonce + 1 != replicaResponse.getNonce()) {
                conflicts++;
                logger.warn("Nonce Conflict");
            } else if (operationType != replicaResponse.getOperationType()) {
                conflicts++;
                logger.warn("Operation Type conflict");
            } else if (!clientResponse.getBody().equals(replicaResponse.getBody())) {
                conflicts++;
                logger.warn("Amount Conflict");
            } else if (replicaResponse.getStatusCode() != 200) {
                conflicts++;
                logger.warn("Status Conflict");
            } else {
                if (!Utils.verifyReplicaResponseSignature(
                        replicaResponse.getReplicaId(),
                        Base64.getDecoder().decode(replicaResponse.getSerializedMessage()),
                        Base64.getDecoder().decode(replicaResponse.getSignature()))
                ) {
                    logger.warn("Signature Conflict");
                    conflicts++;
                }
            }
        }
        return conflicts;
    }

    /**
     * Verifies a replica response signature
     *
     * @param replicaId         Replica Id that responded
     * @param serializedMessage Message serialized
     * @param signature         Signature
     * @return true if the signature is valid, false otherwise
     */
    private static boolean verifyReplicaResponseSignature(int replicaId, byte[] serializedMessage, byte[] signature) {
        try {
            KeyLoader keyLoader = new RSAKeyLoader(replicaId, "config", false, "SHA512withRSA");
            PublicKey pk = keyLoader.loadPublicKey(replicaId);

            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);
            sig.update(serializedMessage);

            return sig.verify(signature);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    /**
     * Insecure Hostname verifier. So the client accepts self-signed certificates
     */
    static public class InsecureHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
