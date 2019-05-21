package rest.server.model;

import java.math.BigInteger;

public class TypedValue {

    private String amount;
    private DataType type;

    public TypedValue() {
    }

    public TypedValue(String amount, DataType type) {
        this.amount = amount;
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public BigInteger getAmountAsBigInteger() {
        return new BigInteger(amount);
    }

    public double getAmountAsDouble() {
        return Double.parseDouble(amount);
    }

    public void setAmountAsDouble(Double amount) {
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
}
