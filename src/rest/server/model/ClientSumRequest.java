package rest.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class ClientSumRequest implements Serializable {

    private String userIdentifier;
    private TypedValue typedValue;
    private String nsquare;
    private Long nonce;
    private String signature;

    public ClientSumRequest() {

    }

    public ClientSumRequest(String userIdentifier, TypedValue typedValue, String nSquare, Long nonce, String signature) {
        this.userIdentifier = userIdentifier;
        this.typedValue = typedValue;
        this.nsquare = nSquare;
        this.nonce = nonce;
        this.signature = signature;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public TypedValue getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(TypedValue typedValue) {
        this.typedValue = typedValue;
    }

    public String getNsquare() {
        return nsquare;
    }

    public void setNsquare(String nsquare) {
        this.nsquare = nsquare;
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

    @JsonIgnore
    public String getSerializeMessage() {
        return userIdentifier + "," + typedValue.getAmount() + ":" + nonce;
    }
}