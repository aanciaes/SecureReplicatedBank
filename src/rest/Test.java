package rest;

import java.security.KeyPair;
import java.util.Base64;
import rest.utils.Utils;

public class Test {

    public static void main(String[] args) {

        try {
            KeyPair kp = Utils.generateNewKeyPair(4096);

            System.out.println(Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
            System.out.println(Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
