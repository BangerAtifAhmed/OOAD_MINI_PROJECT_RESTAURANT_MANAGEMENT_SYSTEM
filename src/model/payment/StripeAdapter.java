package model.payment;

/**
 * Adapter Pattern — Adapter for Stripe (FR-3)
 *
 * Translates the PaymentProcessor interface into StripeAPI calls.
 */
public class StripeAdapter implements PaymentProcessor {

    private final StripeAPI stripeAPI;

    public StripeAdapter() {
        this.stripeAPI = new StripeAPI();
    }

    @Override
    public PaymentResult processPayment(double amount, String orderId) {
        long paise   = Math.round(amount * 100);
        String txnId = stripeAPI.chargeCard(paise, "FoodieFlow Order #" + orderId);
        boolean ok   = stripeAPI.verifyCharge(txnId);
        return new PaymentResult(ok, txnId,
                ok ? "Payment successful via Stripe" : "Stripe charge verification failed");
    }

    @Override
    public String getProviderName() { return "STRIPE"; }
}
