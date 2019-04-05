package rest.server.model;

public class ClientTransferRequest {

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
}
