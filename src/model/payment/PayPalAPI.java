package model.payment;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter Pattern — Adaptee (mock PayPal external SDK)
 *
 * Has a completely different interface from StripeAPI.
 * The adapter hides this incompatibility from the rest of the app.
 */
public class PayPalAPI {

    /** Creates and approves a PayPal payment. Returns a status map. */
    public Map<String, String> createPayment(String currency, double total, String description) {
        System.out.println("[PayPalAPI] Creating payment of " + currency + " " + total + "  — " + description);
        Map<String, String> result = new HashMap<>();
        result.put("paymentId", "paypal_" + System.currentTimeMillis());
        result.put("status",    "APPROVED");
        return result;
    }
}
