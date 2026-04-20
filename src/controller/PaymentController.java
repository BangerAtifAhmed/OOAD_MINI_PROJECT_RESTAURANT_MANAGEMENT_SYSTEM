package controller;

import dao.OrderDAO;
import dao.PaymentDAO;
import model.Order;
import model.Payment;
import model.payment.*;
import singleton.FloorManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MVC Controller — Integrated payment processing (FR-3, FR-7).
 *
 * Selects the correct PaymentProcessor (Adapter) based on method choice,
 * records the payment, releases the table, and generates a receipt file.
 */
public class PaymentController {

    private final PaymentDAO   paymentDAO;
    private final OrderDAO     orderDAO;
    private final FloorManager floorManager;

    public PaymentController() {
        this.paymentDAO  = new PaymentDAO();
        this.orderDAO    = new OrderDAO();
        this.floorManager = FloorManager.getInstance();
    }

    /**
     * Process payment for a SERVED order.
     *
     * @param orderId       the order to pay for
     * @param method        "CASH" | "STRIPE" | "PAYPAL"
     * @return the saved Payment object
     */
    public Payment processPayment(int orderId, String method) throws SQLException, IOException {
        Order order = orderDAO.getById(orderId);
        if (order == null)
            throw new IllegalArgumentException("Order #" + orderId + " not found.");
        if (!"SERVED".equals(order.getStatus()))
            throw new IllegalStateException("Only SERVED orders can be billed.");

        // Select processor via Adapter pattern
        PaymentProcessor processor = selectProcessor(method);
        PaymentResult    result    = processor.processPayment(order.getTotalPrice(), String.valueOf(orderId));

        if (!result.isSuccess())
            throw new RuntimeException("Payment failed: " + result.getMessage());

        String receiptNo = "RCP-" + orderId + "-" + System.currentTimeMillis() % 100000;

        Payment payment = new Payment(
                orderId,
                order.getTotalPrice(),
                processor.getProviderName(),
                result.getTransactionId(),
                receiptNo,
                "SUCCESS"
        );
        paymentDAO.save(payment);

        // Release the table
        floorManager.releaseTable(order.getTableId());

        // FR-7: Generate receipt text file
        generateReceipt(order, payment);

        return payment;
    }

    // ── Adapter selection ─────────────────────────────────────────────────────

    private PaymentProcessor selectProcessor(String method) {
        switch (method.toUpperCase()) {
            case "STRIPE": return new StripeAdapter();
            case "PAYPAL": return new PayPalAdapter();
            default:       return new CashPaymentProcessor();
        }
    }

    // ── Receipt generation (FR-7) ─────────────────────────────────────────────

    private void generateReceipt(Order order, Payment payment) throws IOException {
        String filename = "receipts/receipt_" + payment.getReceiptNumber() + ".txt";
        new java.io.File("receipts").mkdirs();

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            String line = "=".repeat(46);
            String dline = "-".repeat(46);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy  HH:mm");

            pw.println(line);
            pw.println("           F O O D I E F L O W");
            pw.println("         Restaurant Management System");
            pw.println(line);
            pw.printf("Receipt No : %s%n", payment.getReceiptNumber());
            pw.printf("Order No   : #%d%n", order.getId());
            pw.printf("Table      : %d%n", order.getTableId());
            pw.printf("Waiter     : %s%n", order.getWaiterId() == null ? "N/A" : order.getWaiterId());
            pw.printf("Date       : %s%n", LocalDateTime.now().format(fmt));
            pw.println(dline);
            pw.printf("%-28s %6s %8s%n", "Item", "Qty", "Amount");
            pw.println(dline);
            for (var item : order.getItems()) {
                pw.printf("%-28s %6d %8.2f%n",
                        item.getItemName().length() > 28
                            ? item.getItemName().substring(0, 25) + "..."
                            : item.getItemName(),
                        item.getQuantity(),
                        item.getItemTotal());
            }
            pw.println(dline);
            pw.printf("%-34s %8.2f%n", "TOTAL", order.getTotalPrice());
            pw.println(dline);
            pw.printf("Payment    : %s%n", payment.getPaymentMethod());
            pw.printf("Txn ID     : %s%n", payment.getTransactionId());
            pw.printf("Status     : %s%n", payment.getStatus());
            pw.println(line);
            pw.println("        Thank you for dining with us!");
            pw.println(line);
        }
        System.out.println("Receipt saved: " + filename);
    }
}
