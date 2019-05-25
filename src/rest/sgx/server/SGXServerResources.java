package rest.sgx.server;

import hlib.hj.mlib.HomoOpeInt;
import java.security.PrivateKey;
import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.sgx.model.SGXClientSumRequest;
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
    public void compare(SGXClientSumRequest sgxClientRequest) {

    }

    @Override
    public void set_conditional(SGXClientSumRequest sgxClientRequest) {

    }

    @Override
    public void add_conditional(SGXClientSumRequest sgxClientRequest) {

    }
}
