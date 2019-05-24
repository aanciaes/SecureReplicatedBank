package rest.serverController.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class AdminServerRequest implements Serializable {

    private int serverId;
    private int serverPort;
    private boolean debug;
    private boolean unpredictable;
    private boolean testMode;
    private String signature;
    private long nonce;
    private int faults;

    public AdminServerRequest(){

    }
    public AdminServerRequest(int serverId, int serverPort,
                              boolean debug, boolean unpredictable, boolean testMode, int faults) {
        this.serverId = serverId;
        this.serverPort = serverPort;
        this.debug = debug;
        this.unpredictable = unpredictable;
        this.testMode = testMode;
        this.faults = faults;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isUnpredictable() {
        return unpredictable;
    }

    public void setUnpredictable(boolean unpredictable) {
        this.unpredictable = unpredictable;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getFaults() {
        return faults;
    }

    public void setFaults(int faults) {
        this.faults = faults;
    }

    @JsonIgnore
    public String getSerializeMessage() {
        return serverId + ":" + serverPort + ":" + debug + ":" + unpredictable + ":" + testMode + ":" + nonce + ":" + faults;
    }
}
