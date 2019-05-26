package rest.utils;

import java.io.Serializable;

public class Updates implements Serializable {

    private int op;
    private String updKey;
    private String value;

    public Updates() {
    }

    public Updates(int op, String updKey, String value) {
        this.op = op;
        this.updKey = updKey;
        this.value = value;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public String getUpdKey() {
        return updKey;
    }

    public void setUpdKey(String updKey) {
        this.updKey = updKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}