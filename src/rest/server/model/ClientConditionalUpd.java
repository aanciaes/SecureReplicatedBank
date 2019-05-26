package rest.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import rest.utils.Update;

public class ClientConditionalUpd implements Serializable {

    private String condKey;
    private Double condValue;
    private List<Update> updatesList = new ArrayList<>();
    private int condition;
    private Long nonce;

    public ClientConditionalUpd() {
    }

    public ClientConditionalUpd(String condKey, Double condValue, List<Update> updatesList, int condition, Long nonce) {
        this.condKey = condKey;
        this.condValue = condValue;
        this.nonce = nonce;
        this.updatesList = updatesList;
        this.condition = condition;
    }

    public String getCondKey() {
        return condKey;
    }

    public void setCondKey(String condKey) {
        this.condKey = condKey;
    }

    public Double getCondValue() {
        return condValue;
    }

    public void setCondValue(Double condValue) {
        this.condValue = condValue;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public List<Update> getUpdatesList() {
        return updatesList;
    }

    public void setUpdatesList(List<Update> updatesList) {
        this.updatesList = updatesList;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }
}

