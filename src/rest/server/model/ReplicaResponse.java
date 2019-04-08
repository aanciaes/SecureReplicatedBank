package rest.server.model;

import java.io.Serializable;
import java.util.Base64;

/**
 * Response that the replies that the replicas send to the http wallet server
 */
public class ReplicaResponse implements Serializable {

    private int statusCode;
    private String message;
    private Object body;

    // Client check
    private int replicaId;
    private WalletOperationType operationType;
    private byte[] serializedMessage;
    private byte[] signature;
    private long nonce;

    public ReplicaResponse() {
    }

    public ReplicaResponse(int statusCode, String message, Object body, long nonce, WalletOperationType operationType) {
        this.statusCode = statusCode;
        this.message = message;
        this.body = body;
        this.nonce = nonce;
        this.operationType = operationType;
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

    public String getSerializedMessage() {
        return Base64.getEncoder().encodeToString(serializedMessage);
    }

    public void setSerializedMessage(byte[] serializedMessage) {
        this.serializedMessage = serializedMessage;
    }

    public String getSignature() {
        return Base64.getEncoder().encodeToString(signature);
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    public WalletOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(WalletOperationType operationType) {
        this.operationType = operationType;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}