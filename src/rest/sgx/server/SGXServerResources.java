package rest.sgx.server;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.HomoOpeInt;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

            byte[] decryptedKey = Utils.decrypt(privateKey, keyBytes);

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
            List<String> rst = new ArrayList();
            PrivateKey privateKey = AdminSgxKeyLoader.loadPrivateKey("sgxPrivateKey.pem");
            byte[] keyBytes = Base64.getDecoder().decode(sgxClientRequest.getKey());
            byte[] decryptedKey = Utils.decrypt(privateKey, keyBytes);

            Integer highest = sgxClientRequest.getHighest().intValue();
            Integer lowest = sgxClientRequest.getLowest().intValue();

            sgxClientRequest.getDbServer().forEach((String key, BigInteger value) -> {
                try{
                    BigInteger decryptValue = HomoAdd.decrypt(value, HomoAdd.keyFromString(Base64.getEncoder().encodeToString(decryptedKey)));

                    if (decryptValue.intValue() <= highest && decryptValue.intValue() >= lowest) {
                        rst.add(key);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            });
            System.out.println(rst.size());
            return new SGXResponse(200, rst);
        } catch (Exception e) {
            e.printStackTrace();
            return new SGXResponse(500, "Internal Server Error");
        }
    }

    @Override
    public void compare(SGXConditionalUpdateRequest sgxClientRequest) {

    }
}
