package rest.sgx.model;

import rest.server.model.TypedValue;

import java.io.Serializable;

public class SGXApplyUpdateRequest implements Serializable {

    private TypedValue typedValue;
    private String value;
    private int operation;

    public SGXApplyUpdateRequest() {
    }

    public SGXApplyUpdateRequest(TypedValue typedValue, String value, int operation) {
        this.typedValue = typedValue;
        this.value = value;
        this.operation = operation;
    }

    public TypedValue getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(TypedValue typedValue) {
        this.typedValue = typedValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }
}