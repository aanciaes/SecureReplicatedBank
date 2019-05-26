package rest.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.math.BigInteger;

public class TypedValue implements Serializable {

    private String amount;
    private DataType type;
    private String encodedHomoKey;
    private String encodedSymKey;

    public TypedValue() {
    }

    public TypedValue(String amount, DataType type, String encodedHomoKey, String encodedSymKey) {
        this.amount = amount;
        this.type = type;
        this.encodedHomoKey = encodedHomoKey;
        this.encodedSymKey = encodedSymKey;
    }

    public String getAmount() {
        return amount;
    }

    @JsonIgnore
    public BigInteger getAmountAsBigInteger() {
        return new BigInteger(amount);
    }

    @JsonIgnore
    public double getAmountAsDouble() {
        return Double.parseDouble(amount);
    }

    @JsonIgnore
    public void setAmountAsDouble(Double amount) {
        this.amount = amount.toString();
    }

    @JsonIgnore
    public void setAmountAsBigInteger(BigInteger amount) {
        this.amount = amount.toString();
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @JsonIgnore
    public long getAmountAsLong() {
        return Long.parseLong(this.amount);
    }

    public String getEncodedHomoKey() {
        return encodedHomoKey;
    }

    public void setEncodedHomoKey(String encodedHomoKey) {
        this.encodedHomoKey = encodedHomoKey;
    }

    public String getEncodedSymKey() {
        return encodedSymKey;
    }

    public void setEncodedSymKey(String encodedSymKey) {
        this.encodedSymKey = encodedSymKey;
    }
}
