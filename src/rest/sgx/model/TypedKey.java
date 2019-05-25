package rest.sgx.model;

import rest.server.model.DataType;

public class TypedKey {

    private DataType type;
    private String key;

    public TypedKey() {
    }

    public TypedKey(DataType type, String key) {
        this.type = type;
        this.key = key;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
