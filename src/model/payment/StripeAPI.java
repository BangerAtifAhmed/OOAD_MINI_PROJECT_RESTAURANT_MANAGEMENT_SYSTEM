package model.payment;

/**
 * Adapter Pattern — Adaptee (mock Stripe external SDK)
 *
 * Simulates a third-party API with its own method signatures.
 * The rest of the app should never call this class directly.
 */
public class StripeAPI {

    /** Charges a card. Amount is in smallest currency unit (paise for INR). */
    public String chargeCard(long amountInPaise, String description) {
        // Mock: always succeeds and returns a transaction ID
        System.out.println("[StripeAPI] Charging ₹" + (amountInPaise / 100.0) + "  — " + description);
        return "stripe_txn_" + System.currentTimeMillis();
    }

    /** Verifies a charge ID looks legitimate */
    public boolean verifyCharge(String chargeId) {
        return chargeId != null && chargeId.startsWith("stripe_txn_");
    }
}
