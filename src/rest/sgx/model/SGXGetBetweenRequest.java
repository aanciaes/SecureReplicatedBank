package rest.sgx.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SGXGetBetweenRequest implements Serializable {

    private Map<String, BigInteger> dbServer = new HashMap<>();
    private BigInteger lowest;
    private BigInteger highest;
    private String key;

    public SGXGetBetweenRequest() {
    }

    public SGXGetBetweenRequest(Map<String, BigInteger> dbServer, BigInteger lowest, BigInteger highest, String key) {
        this.dbServer = dbServer;
        this.lowest = lowest;
        this.highest = highest;
        this.key = key;
    }

    public Map<String, BigInteger> getDbServer() {
        return dbServer;
    }

    public void setDbServer(Map<String, BigInteger> dbServer) {
        this.dbServer = dbServer;
    }

    public BigInteger getLowest() {
        return lowest;
    }

    public void setLowest(BigInteger lowest) {
        this.lowest = lowest;
    }

    public BigInteger getHighest() {
        return highest;
    }

    public void setHighest(BigInteger highest) {
        this.highest = highest;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}