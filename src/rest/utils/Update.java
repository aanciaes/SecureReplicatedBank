package rest.utils;

import java.io.Serializable;

public class Update implements Serializable {

    private int op;
    private String updKey;
    private String value;

    // for sum operation, having the nsquare here saves a request do the sgx secure model
    private String nsquare;

    public Update() {
    }

    public Update(int op, String updKey, String value, String nsquare) {
        this.op = op;
        this.updKey = updKey;
        this.value = value;
        this.nsquare = nsquare;
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

    public String getNsquare() {
        return nsquare;
    }

    public void setNsquare(String nsquare) {
        this.nsquare = nsquare;
    }
}