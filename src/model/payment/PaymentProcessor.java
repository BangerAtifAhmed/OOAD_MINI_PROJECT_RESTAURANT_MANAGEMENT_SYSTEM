package model.payment;

/**
 * Adapter Pattern — Target Interface (FR-3)
 *
 * The billing module depends only on this interface (DIP / LSP).
 * Any payment provider (Stripe, PayPal, Cash) that implements it
 * can be swapped in without touching the BillingPanel or PaymentController.
 */
public interface PaymentProcessor {
    PaymentResult processPayment(double amount, String orderId);
    String getProviderName();
}
