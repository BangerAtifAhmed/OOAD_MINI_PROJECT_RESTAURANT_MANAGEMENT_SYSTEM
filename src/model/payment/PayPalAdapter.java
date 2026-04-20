package model.payment;

import java.util.Map;

/**
 * Adapter Pattern — Adapter for PayPal (FR-3)
 *
 * Translates the PaymentProcessor interface into PayPalAPI calls.
 */
public class PayPalAdapter implements PaymentProcessor {

    private final PayPalAPI payPalAPI;

    public PayPalAdapter() {
        this.payPalAPI = new PayPalAPI();
    }

    @Override
    public PaymentResult processPayment(double amount, String orderId) {
        Map<String, String> result =
                payPalAPI.createPayment("INR", amount, "FoodieFlow Order #" + orderId);
        boolean ok = "APPROVED".equals(result.get("status"));
        return new PaymentResult(ok, result.get("paymentId"),
                ok ? "Payment successful via PayPal" : "PayPal payment was not approved");
    }

    @Override
    public String getProviderName() { return "PAYPAL"; }
}
