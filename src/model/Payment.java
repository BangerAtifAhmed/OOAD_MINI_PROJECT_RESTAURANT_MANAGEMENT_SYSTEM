package model;

import java.time.LocalDateTime;

public class Payment {
    private int id;
    private int orderId;
    private double amount;
    private String paymentMethod;
    private String transactionId;
    private String receiptNumber;
    private String status;
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(int orderId, double amount, String paymentMethod,
                   String transactionId, String receiptNumber, String status) {
        this.orderId       = orderId;
        this.amount        = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.receiptNumber = receiptNumber;
        this.status        = status;
    }

    // Getters
    public int    getId()              { return id; }
    public int    getOrderId()         { return orderId; }
    public double getAmount()          { return amount; }
    public String getPaymentMethod()   { return paymentMethod; }
    public String getTransactionId()   { return transactionId; }
    public String getReceiptNumber()   { return receiptNumber; }
    public String getStatus()          { return status; }
    public LocalDateTime getPaidAt()   { return paidAt; }

    // Setters
    public void setId(int id)                         { this.id = id; }
    public void setOrderId(int orderId)               { this.orderId = orderId; }
    public void setAmount(double amount)              { this.amount = amount; }
    public void setPaymentMethod(String pm)           { this.paymentMethod = pm; }
    public void setTransactionId(String tid)          { this.transactionId = tid; }
    public void setReceiptNumber(String rn)           { this.receiptNumber = rn; }
    public void setStatus(String status)              { this.status = status; }
    public void setPaidAt(LocalDateTime paidAt)       { this.paidAt = paidAt; }

    @Override
    public String toString() {
        return String.format("Receipt %s | Order #%d | %s | ₹%.2f | %s",
                receiptNumber, orderId, paymentMethod, amount, status);
    }
}
