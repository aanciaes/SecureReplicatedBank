package rest.server.model;

import java.io.Serializable;

/**
 * Input class of the add money request
 */
public class ClientAddMoneyRequest implements Serializable {
    private String toPubKey;
    private TypedValue typedValue;
    private Long nonce;
    private String signature;

    public ClientAddMoneyRequest() {
    }

    public ClientAddMoneyRequest(String toPubKey, TypedValue typedValue, Long nonce, String signature) {
        this.toPubKey = toPubKey;
        this.typedValue = typedValue;
        this.nonce = nonce;
        this.signature = signature;
    }

    public String getToPubKey() {
        return toPubKey;
    }

    public void setToPubKey(String toPubKey) {
        this.toPubKey = toPubKey;
    }

    public TypedValue getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(TypedValue typedValue) {
        this.typedValue = typedValue;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSerializeMessage() {
        return toPubKey + "," + typedValue.getAmount() + ":" + nonce;
    }
}
