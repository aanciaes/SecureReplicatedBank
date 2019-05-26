package rest.sgx.model;

import rest.server.model.TypedValue;
import java.io.Serializable;

public class SGXClientSumRequest implements Serializable {

    private TypedKey typedKey;
    private long amount1;
    private long amount2;

    public SGXClientSumRequest() {
    }

    public SGXClientSumRequest(TypedKey typedKey, long amount1, long amount2) {
        this.typedKey = typedKey;
        this.amount1 = amount1;
        this.amount2 = amount2;
    }

    public TypedKey getTypedKey() {
        return typedKey;
    }

    public void setTypedKey(TypedKey typedKey) {
        this.typedKey = typedKey;
    }

    public long getAmount1() {
        return amount1;
    }

    public void setAmount1(long amount1) {
        this.amount1 = amount1;
    }

    public long getAmount2() {
        return amount2;
    }

    public void setAmount2(long amount2) {
        this.amount2 = amount2;
    }
}
