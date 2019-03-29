package rest.server.model;

public class User {

    private Long userId;
    private Double amount;

    public User(Long userId, Double amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void addMoney(Double amount) {
        this.amount += amount;
    }

    public void substractMoney(Double amount) {
        this.amount -= amount;
    }

    public boolean canTransfer(Double amount) {
        return amount > 0 && this.amount - amount >= 0.0;
    }
}
