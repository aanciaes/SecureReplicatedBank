package rest.sgx.model;

import rest.server.model.TypedValue;
import java.io.Serializable;

public class SGXClientRequest implements Serializable {

    private String clientID;
    private String clientKey;
    private TypedValue typedValue;

    public SGXClientRequest() {
    }

    public SGXClientRequest(String clientID, String clientKey, TypedValue typedValue) {
        this.clientID = clientID;
        this.clientKey = clientKey;
        this.typedValue = typedValue;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public TypedValue getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(TypedValue typedValue) {
        this.typedValue = typedValue;
    }
}
