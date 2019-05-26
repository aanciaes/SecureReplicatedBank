package rest.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import rest.utils.Updates;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class ClientConditionalUpd implements Serializable {

    private PublicKey publicKey;
    private TypedValue typedValue;
    private String nsquare;
    private Long nonce;
    private String signature;
    private List<Updates> updatesList = new ArrayList<>();
    private int condition;


    public ClientConditionalUpd() {
    }

    public ClientConditionalUpd(PublicKey publicKey, TypedValue typedValue, String nsquare, Long nonce, String signature, List<Updates> updatesList, int condition) {
        this.publicKey = publicKey;
        this.typedValue = typedValue;
        this.nsquare = nsquare;
        this.nonce = nonce;
        this.signature = signature;
        this.updatesList = updatesList;
        this.condition = condition;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
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

    public List<Updates> getUpdatesList() {
        return updatesList;
    }

    public void setUpdatesList(List<Updates> updatesList) {
        this.updatesList = updatesList;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    @JsonIgnore
    public String getSerializeMessage() {
        return publicKey + "," + typedValue.getAmount() + "," + nsquare + ":" + getUpdatesList().toString() + ":" + nonce;
    }
}

