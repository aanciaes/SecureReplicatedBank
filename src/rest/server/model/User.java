package rest.server.model;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.util.KeyLoader;

import java.io.Serializable;
import java.security.PublicKey;

public class User implements Serializable {

    private PublicKey pubK;
    private Double amount;

    public User(int userId, Double amount){

        this.amount = amount;
        KeyLoader keyLoader = new RSAKeyLoader(userId + 1000, "config", false, "SHA256withRSA");

        try {
            this.pubK = keyLoader.loadPublicKey(userId + 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return this.pubK;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void addMoney(Double amount) {
        this.amount += amount;
    }

    public void substractMoney(Double amount) {
        this.amount -= amount;
    }

    public boolean canTransfer(Double amount) {
        return amount > 0 && this.amount - amount >= 0.0;
    }
}
