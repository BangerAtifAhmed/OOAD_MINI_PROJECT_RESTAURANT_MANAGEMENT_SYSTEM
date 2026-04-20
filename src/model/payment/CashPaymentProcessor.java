package model.payment;

/** No adapter needed — cash is handled natively */
public class CashPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(double amount, String orderId) {
        System.out.println("[Cash] ₹" + amount + " received for Order #" + orderId);
        return new PaymentResult(true, "CASH_" + System.currentTimeMillis(), "Cash payment recorded");
    }

    @Override
    public String getProviderName() { return "CASH"; }
}
