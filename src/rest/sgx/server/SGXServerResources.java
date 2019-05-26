package rest.sgx.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import hlib.hj.mlib.HelpSerial;
import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;
import hlib.hj.mlib.PaillierKey;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.server.model.TypedValue;
import rest.sgx.model.GetBetweenResponse;
import rest.sgx.model.SGXClientSumRequest;
import rest.sgx.model.SGXConditionalUpdateRequest;
import rest.sgx.model.SGXGetBetweenRequest;
import rest.sgx.model.SGXResponse;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Utils;

public class SGXServerResources implements SGXServerInterface {

    private static Logger logger = LogManager.getLogger(SGXServerResources.class.getName());

    @Override
    public synchronized SGXResponse sum(SGXClientSumRequest sgxClientRequest) {
        logger.info("Protected sum operation");

        try {
            PrivateKey privateKey = AdminSgxKeyLoader.loadPrivateKey("sgxPrivateKey.pem");
            byte[] keyBytes = Base64.getDecoder().decode(sgxClientRequest.getTypedKey().getKey());

            byte[] decryptedKey = Utils.decrypt("RSA", "SunJCE", privateKey, keyBytes);

            HomoOpeInt opeInt = new HomoOpeInt(new String(decryptedKey));

            long amount1Dec = opeInt.decrypt(sgxClientRequest.getAmount1());
            long amount2Dec = opeInt.decrypt(sgxClientRequest.getAmount2());

            return new SGXResponse(200, Long.toString(opeInt.encrypt((int) (amount1Dec + amount2Dec))));
        } catch (Exception e) {
            e.printStackTrace();
            return new SGXResponse(500, "Internal Server Error");
        }
    }

    @Override
    public synchronized SGXResponse getBetween(SGXGetBetweenRequest sgxClientRequest) {
        logger.info("Protected getBetween operation");

        try {
            List<String> rst = new ArrayList<>();
            Map<String, TypedValue> db = sgxClientRequest.getDbServer();

            PrivateKey privateKey = AdminSgxKeyLoader.loadPrivateKey("sgxPrivateKey.pem");

            int highest = sgxClientRequest.getHighest().intValue();
            int lowest = sgxClientRequest.getLowest().intValue();

            System.out.println(sgxClientRequest.getDbServer().size());
            db.forEach((String key, TypedValue value) -> {
                try {
                    byte[] symKey = Base64.getDecoder().decode(value.getEncodedSymKey());
                    byte[] paillierKey = Base64.getDecoder().decode(value.getEncodedHomoKey());

                    byte[] decryptedSymKey = Utils.decrypt("RSA", "SunJCE", privateKey, symKey);

                    SecretKey secretKey = new SecretKeySpec(decryptedSymKey, 0, decryptedSymKey.length, "AES");
                    byte[] decryptedPaillierKeyBytes = Utils.decrypt("AES", "SunJCE", secretKey, paillierKey);

                    PaillierKey pk = (PaillierKey) HelpSerial.fromString(new String(decryptedPaillierKeyBytes));

                    BigInteger decryptValue = HomoAdd.decrypt(value.getAmountAsBigInteger(), pk);

                    if (decryptValue.intValue() <= highest && decryptValue.intValue() >= lowest) {
                        rst.add(key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return new SGXResponse(200, new ObjectMapper().writer().writeValueAsString(new GetBetweenResponse(rst)));
        } catch (Exception e) {
            e.printStackTrace();
            return new SGXResponse(500, "Internal Server Error");
        }
    }

    @Override
    public synchronized boolean compare(SGXConditionalUpdateRequest sgxClientRequest) {
        TypedValue tv = sgxClientRequest.getTypedValue();

        switch (tv.getType()) {
            case HOMO_ADD:
                int homoAddBalance = getHomoAddBalance(tv).intValue();
                return checkCondition(homoAddBalance, sgxClientRequest.getCondValue(), sgxClientRequest.getCondition());
            case HOMO_OPE_INT:
                int opeIntBalance = getHomoOpeIntBalance(tv);
                return checkCondition(opeIntBalance, sgxClientRequest.getCondValue(), sgxClientRequest.getCondition());
            default:
                return false;
        }
    }

    private BigInteger getHomoAddBalance(TypedValue typedValue) {
        try {
            PrivateKey privateKey = AdminSgxKeyLoader.loadPrivateKey("sgxPrivateKey.pem");

            byte[] symKey = Base64.getDecoder().decode(typedValue.getEncodedSymKey());
            byte[] paillierKey = Base64.getDecoder().decode(typedValue.getEncodedHomoKey());

            byte[] decryptedSymKey = Utils.decrypt("RSA", "SunJCE", privateKey, symKey);

            SecretKey secretKey = new SecretKeySpec(decryptedSymKey, 0, decryptedSymKey.length, "AES");
            byte[] decryptedPaillierKeyBytes = Utils.decrypt("AES", "SunJCE", secretKey, paillierKey);

            PaillierKey pk = (PaillierKey) HelpSerial.fromString(new String(decryptedPaillierKeyBytes));

            return HomoAdd.decrypt(typedValue.getAmountAsBigInteger(), pk);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Integer getHomoOpeIntBalance(TypedValue typedValue) {
        try {
            PrivateKey privateKey = AdminSgxKeyLoader.loadPrivateKey("sgxPrivateKey.pem");
            byte[] keyBytes = Base64.getDecoder().decode(typedValue.getEncodedHomoKey());

            byte[] decryptedKey = Utils.decrypt("RSA", "SunJCE", privateKey, keyBytes);

            HomoOpeInt opeInt = new HomoOpeInt(new String(decryptedKey));

            return opeInt.decrypt(typedValue.getAmountAsLong());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkCondition(int balance, Double condValue, int condition) {
        switch (condition) {
            case 0:
                return balance == condValue;
            case 1:
                return balance != condValue;
            case 2:
                return balance > condValue;
            case 3:
                return balance >= condValue;
            case 4:
                return balance < condValue;
            case 5:
                return balance <= condValue;
            default:
                return false;
        }
    }
}
