package rest.server.model;

import java.io.Serializable;

/**
 * Input class of the transfer money request
 */
public class ClientTransferRequest implements Serializable {

    private String fromPubKey;
    private String toPubKey;
    private TypedValue tv;
    private Long nonce;
    private String signature;

    public ClientTransferRequest() {
    }

    public ClientTransferRequest(String fromPubKey, String toPubKey, TypedValue tv, Long nonce, String signature) {
        this.fromPubKey = fromPubKey;
        this.toPubKey = toPubKey;
        this.tv = tv;
        this.nonce = nonce;
        this.signature = signature;
    }

    public String getFromPubKey() {
        return fromPubKey;
    }

    public String getToPubKey() {
        return toPubKey;
    }

    public String getAmount() {
        return tv.getAmount();
    }

    public Long getNonce() {
        return nonce;
    }

    public String getSignature() {
        return signature;
    }

    public void setFromPubKey(String fromPubKey) {
        this.fromPubKey = fromPubKey;
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
        return fromPubKey + "," + toPubKey + "," + tv.getAmount() + ":" + nonce;
    }

    public TypedValue getTypedValue() {
        return tv;
    }

}
