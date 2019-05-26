package rest.utils;

import java.security.PublicKey;

public class Updates {

    private String op;
    private PublicKey key;
    private String value;

    public Updates() {
    }

    public Updates(String op, PublicKey key, String value) {
        this.op = op;
        this.key = key;
        this.value = value;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public PublicKey getKey() {
        return key;
    }

    public void setKey(PublicKey key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
