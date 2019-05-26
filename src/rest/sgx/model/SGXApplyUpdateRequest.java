package rest.sgx.model;

import rest.server.model.TypedValue;

public class SGXApplyUpdateRequest {

    private int operation;
    private int value;
    private TypedValue typedValue;

    public SGXApplyUpdateRequest() {
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public TypedValue getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(TypedValue typedValue) {
        this.typedValue = typedValue;
    }
}