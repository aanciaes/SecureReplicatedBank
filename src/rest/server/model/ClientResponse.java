package rest.server.model;

import java.io.Serializable;
import java.util.List;

/**
 * Response that the http wallet server returns to the clients
 */
public class ClientResponse implements Serializable {

    private Object body;
    private List<ReplicaResponse> responses;

    public ClientResponse() {
    }

    public ClientResponse(Object body, List<ReplicaResponse> responses) {
        this.body = body;
        this.responses = responses;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public List<ReplicaResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<ReplicaResponse> responses) {
        this.responses = responses;
    }
}