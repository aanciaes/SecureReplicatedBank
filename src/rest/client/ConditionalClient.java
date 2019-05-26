package rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.client.get.GetBalanceClient;
import rest.server.model.*;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Updates;
import rest.utils.Utils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;

import static rest.client.create.CreateHomoAddClient.generateAES;

public class ConditionalClient {

    private static Logger logger = LogManager.getLogger(GetBalanceClient.class.getName());

    public static void conditional_upd(WebTarget target, int faults, KeyPair kp, String amount, String s1, List<Updates> updates, DataType dataType, String key) {
        try {
            ClientConditionalUpd clientRequest = new ClientConditionalUpd();
            clientRequest.setPublicKey(kp.getPublic());

            PaillierKey paillierKey = null;
            HomoOpeInt homoOpeInt = null;
            TypedValue clientTv = new TypedValue(amount, dataType, null, null);
            Key symKey = generateAES();
            if (dataType == DataType.HOMO_ADD) {
                paillierKey = (PaillierKey) HelpSerial.fromString(key);
                amount = HomoAdd.encrypt(new BigInteger(amount), paillierKey).toString();
                clientRequest.setNsquare(paillierKey.getNsquare().toString());

                byte [] encryptedHomoAddKeyBytes = Utils.encryptMessage("AES", "SunJCE", symKey, key.getBytes());
                String encryptedPallierKey = Base64.getEncoder().encodeToString(encryptedHomoAddKeyBytes);

                byte[] encryptedSymKeyBytes = Utils.encryptMessage("RSA", "SunJCE", AdminSgxKeyLoader.loadPublicKey("sgxPublicKey.pem"), symKey.getEncoded());
                String encryptedSymKey = Base64.getEncoder().encodeToString(encryptedSymKeyBytes);

                clientTv = new TypedValue (amount, dataType, encryptedPallierKey, encryptedSymKey);
            }else if(dataType == DataType.HOMO_OPE_INT){
                homoOpeInt = new HomoOpeInt(key);
                amount = ((Long) homoOpeInt.encrypt(Integer.parseInt(amount))).toString();
                byte[] encryptedSymKeyBytes = Utils.encryptMessage("RSA", "SunJCE", AdminSgxKeyLoader.loadPublicKey("sgxPublicKey.pem"), symKey.getEncoded());
                String encryptedSymKey = Base64.getEncoder().encodeToString(encryptedSymKeyBytes);
                clientTv = new TypedValue (amount, dataType, null, encryptedSymKey);
            }

            clientRequest.setTypedValue(clientTv);

            // Nonce to randomise message encryption
            clientRequest.setNonce(Utils.generateNonce());

            byte[] hashedMessage = Utils.hashMessage(clientRequest.getSerializeMessage().getBytes());
            byte[] encryptedHash = Utils.encryptMessage("RSA", "SunJCE", kp.getPrivate(), hashedMessage);

            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));

            long nonce = Utils.generateNonce();
            clientRequest.setSignature(Base64.getEncoder().encodeToString(encryptedHash));
            Gson gson = new Gson();
            String json = gson.toJson(clientRequest);

            Response response = target.path("/conditional_upd").request().header("nonce", nonce)
                    .post(Entity.entity(json, MediaType.APPLICATION_JSON));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
