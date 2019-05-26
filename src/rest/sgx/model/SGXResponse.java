package rest.sgx.model;

import java.io.Serializable;

public class SGXResponse implements Serializable {

    private int statusCode;
    private Object body;

    public SGXResponse() {
    }

    public SGXResponse(int statusCode, Object body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
