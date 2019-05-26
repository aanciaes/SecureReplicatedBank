package rest.sgx.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import rest.server.model.TypedValue;

public class SGXGetBetweenRequest implements Serializable {

    private Map<String, TypedValue> dbServer = new HashMap<>();
    private BigInteger lowest;
    private BigInteger highest;

    public SGXGetBetweenRequest() {
    }

    public SGXGetBetweenRequest(Map<String, TypedValue> dbServer, BigInteger lowest, BigInteger highest) {
        this.dbServer = dbServer;
        this.lowest = lowest;
        this.highest = highest;
    }

    public Map<String, TypedValue> getDbServer() {
        return dbServer;
    }

    public void setDbServer(Map<String, TypedValue> dbServer) {
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
}