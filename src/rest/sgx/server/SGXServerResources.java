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
            byte[] keyBytes = Base64.getDecoder().decode(sgxClientRequest.getTypedKey().getKey());

            byte[] decryptedKey = Utils.decrypt(privateKey, keyBytes);

            String homoOpeKey = new String(decryptedKey);
            System.out.println("homoKey: " + homoOpeKey);

            HomoOpeInt opeInt = new HomoOpeInt(homoOpeKey);

            long amount1 = sgxClientRequest.getAmount1();
            long amount2 = sgxClientRequest.getAmount2();

            System.out.println("1 enc: " + amount1);
            System.out.println("2 enc: " + amount2);

            long amount1Dec = opeInt.decrypt(amount1);
            long amount2Dec = opeInt.decrypt(amount2);

            System.out.println("1 dec: " + amount1Dec);
            System.out.println("2 dec: " + amount2Dec);

            Long sum = amount1Dec + amount2Dec;
            System.out.println("sum: " + sum);

            long result = opeInt.encrypt(sum.intValue());
            return Long.toString(result);

        } catch (Exception e) {
            System.out.println("here");
            return "error";
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
