package rest.server.model;

import java.io.Serializable;

public class ApplicationResponse implements Serializable {

    private int statusCode;
    private String message;

    public ApplicationResponse() {
    }

    public ApplicationResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
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
}
