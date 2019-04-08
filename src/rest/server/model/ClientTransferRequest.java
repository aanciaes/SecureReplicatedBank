package rest.server.model;

import java.io.Serializable;

/**
 * Input class of the transfer money request
 */
public class ClientTransferRequest implements Serializable {

    private String fromPubKey;
    private String toPubKey;
    private Double amount;
    private Long nonce;
    private String signature;

    public ClientTransferRequest() {
    }

    public ClientTransferRequest(String fromPubKey, String toPubKey, Double amount, Long nonce, String signature) {
        this.fromPubKey = fromPubKey;
        this.toPubKey = toPubKey;
        this.amount = amount;
        this.nonce = nonce;
        this.signature = signature;
    }

    public String getFromPubKey() {
        return fromPubKey;
    }

    public String getToPubKey() {
        return toPubKey;
    }

    public Double getAmount() {
        return amount;
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

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSerializeMessage() {
        return fromPubKey + "," + toPubKey + "," + amount + ":" + nonce;
    }
}
