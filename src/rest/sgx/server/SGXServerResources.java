package rest.sgx.server;

import hlib.hj.mlib.HomoOpeInt;
import rest.sgx.model.SGXClientSumRequest;
import rest.utils.AdminSgxKeyLoader;
import rest.utils.Utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class SGXServerResources implements SGXServerInterface{

    @Override
    public String sum(SGXClientSumRequest sgxClientRequest) {
        try {
            PrivateKey privateKey = AdminSgxKeyLoader.loadPrivateKey("sgxPrivateKey.pem");
            byte[] decryptedKey = Utils.decrypt(privateKey, sgxClientRequest.getTypedKey().getKey());

            HomoOpeInt opeInt = new HomoOpeInt(Base64.getEncoder().encodeToString(decryptedKey));
            Integer amount1 = opeInt.decrypt(sgxClientRequest.getAmount1());
            Integer amount2 = opeInt.decrypt(sgxClientRequest.getAmount2());

            Long result = opeInt.encrypt((amount1 + amount2));
            return result.toString();

        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return "error";
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
