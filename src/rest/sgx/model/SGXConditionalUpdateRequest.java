package rest.sgx.model;

import rest.server.model.TypedValue;

public class SGXConditionalUpdateRequest {

    private TypedValue typedValue;
    private Double condValue;
    private int condition;

    public SGXConditionalUpdateRequest() {
    }

    public SGXConditionalUpdateRequest(TypedValue typedValue, Double condValue, int condition) {
        this.typedValue = typedValue;
        this.condValue = condValue;
        this.condition = condition;
    }

    public TypedValue getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(TypedValue typedValue) {
        this.typedValue = typedValue;
    }

    public Double getCondValue() {
        return condValue;
    }

    public void setCondValue(Double condValue) {
        this.condValue = condValue;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }
}
