package rest.sgx.model;

import rest.server.model.TypedValue;
import java.io.Serializable;

public class SGXClientSumRequest implements Serializable {

    private TypedKey typedKey;
    private Long amount1;
    private Long amount2;

    public SGXClientSumRequest() {
    }

    public SGXClientSumRequest(TypedKey typedKey, Long amount1, Long amount2) {
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

    public Long getAmount1() {
        return amount1;
    }

    public void setAmount1(Long amount1) {
        this.amount1 = amount1;
    }

    public Long getAmount2() {
        return amount2;
    }

    public void setAmount2(Long amount2) {
        this.amount2 = amount2;
    }
}
