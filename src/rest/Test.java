package rest;

import hlib.hj.mlib.HomoAdd;
import hlib.hj.mlib.PaillierKey;
import java.math.BigInteger;

public class Test {

    public static void main(String[] args) {

        try {
            PaillierKey pk = HomoAdd.generateKey();
            //pk.printValues();

            BigInteger int1 = new BigInteger("1");
            BigInteger int2 = new BigInteger("2");

            BigInteger int1enc = HomoAdd.encrypt(int1, pk);
            BigInteger int2enc = HomoAdd.encrypt(int2, pk);

            BigInteger int1plus2Enc = HomoAdd.sum(int1enc, int2enc, pk.getNsquare());
            System.out.println(int1plus2Enc);
            BigInteger int1plus2 = HomoAdd.decrypt(int1plus2Enc, pk);
            System.out.println(int1plus2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
