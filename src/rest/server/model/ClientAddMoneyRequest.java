package rest.server.model;

import java.io.Serializable;

/**
 * Input class of the add money request
 */
public class ClientAddMoneyRequest implements Serializable {
    private String toPubKey;
    private TypedValue tv;
    private Long nonce;
    private String signature;

    public ClientAddMoneyRequest() {
    }

    public ClientAddMoneyRequest(String toPubKey, TypedValue tv, Long nonce, String signature) {
        this.toPubKey = toPubKey;
        this.tv = tv;
        this.nonce = nonce;
        this.signature = signature;

    }

    public String getToPubKey() {
        return toPubKey;
    }

    public TypedValue getTypedValue() {
        return tv;
    }

    public Long getNonce() {
        return nonce;
    }

    public String getSignature() {
        return signature;
    }

    public void setToPubKey(String toPubKey) {
        this.toPubKey = toPubKey;
    }

    public void setAmount(TypedValue tv) {
        this.tv = tv;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSerializeMessage() {
        return toPubKey + "," + tv.getAmount() + ":" + nonce;
    }
}
