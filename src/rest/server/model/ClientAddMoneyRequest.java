package rest.server.model;

import java.io.Serializable;

/**
 * Input class of the add money request
 */
public class ClientAddMoneyRequest implements Serializable {
    private String toPubKey;
    private Double amount;
    private Long nonce;
    private String signature;

    public ClientAddMoneyRequest() {
    }

    public ClientAddMoneyRequest(String toPubKey, Double amount, Long nonce, String signature) {
        this.toPubKey = toPubKey;
        this.amount = amount;
        this.nonce = nonce;
        this.signature = signature;
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
        return toPubKey + "," + amount + ":" + nonce;
    }
}
