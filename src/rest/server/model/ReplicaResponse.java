package rest.server.model;

import java.io.Serializable;

public class ReplicaResponse implements Serializable {

    private int statusCode;
    private String message;
    private Object body;

    public ReplicaResponse() {
    }

    public ReplicaResponse(int statusCode, String message, Object body) {
        this.statusCode = statusCode;
        this.message = message;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}