package com.example.pos_project.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.pos_project.R;

import java.util.List;

public class SaleCompleteDialog extends Dialog {

    private TextView btnOk;

    // Data received from CartActivity
    private String customerName;
    private double totalAmount;
    private double amountPaid;
    private String paymentMethod;
    private List<com.example.pos_project.model.CartItem> cartItems;

    private OnSaleCompleteListener listener;

    public interface OnSaleCompleteListener {
        void onSaleComplete(String customerName, double totalAmount, double amountPaid, String paymentMethod);
    }

    public SaleCompleteDialog(@NonNull Context context, String customerName, double totalAmount,
                             double amountPaid, String paymentMethod,
                             List<com.example.pos_project.model.CartItem> cartItems,
                             OnSaleCompleteListener listener) {
        super(context);
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_sale_complete);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnOk = findViewById(R.id.btn_ok);
    }

    private void setupClickListeners() {
        btnOk.setOnClickListener(v -> {
            // Auto-show receipt dialog
            dismiss(); // Close current dialog first
            showReceiptDialog();
        });
    }

    private void showReceiptDialog() {
        // Import the receipt dialog logic from CartActivity
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_receipt_checkout, null);

        // Get references to views
        android.widget.LinearLayout itemsContainer = dialogView.findViewById(R.id.items_container);
        android.widget.TextView tvSubtotal = dialogView.findViewById(R.id.tv_receipt_subtotal);
        android.widget.TextView tvSeller = dialogView.findViewById(R.id.tv_receipt_seller);
        android.widget.LinearLayout customerPaymentInfo = dialogView.findViewById(R.id.customer_payment_info);
        android.widget.ImageView btnCloseReceipt = dialogView.findViewById(R.id.btn_close_receipt);

        // Set seller name (user login name)
        String sellerName = com.example.pos_project.auth.AuthManager.getInstance(getContext()).getUserName();
        if (sellerName != null && !sellerName.isEmpty()) {
            tvSeller.setText(sellerName);
        } else {
            tvSeller.setText("Unknown");
        }

        // Set subtotal
        tvSubtotal.setText(String.format("$%.2f", totalAmount));

        // Populate items list - we'll need to get cart items from CartManager
        populateReceiptItems(itemsContainer);

        // Add customer name and amount paid display
        android.widget.TextView tvCustLabel = new android.widget.TextView(getContext());
        tvCustLabel.setText("Customer: " + customerName);
        tvCustLabel.setTextSize(14);
        tvCustLabel.setTextColor(getContext().getResources().getColor(android.R.color.black));
        tvCustLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCustLabel.setPadding(0, 0, 0, 8);

        android.widget.TextView tvAmountLabel = new android.widget.TextView(getContext());
        tvAmountLabel.setText("Amount Paid: $" + String.format("%.2f", amountPaid));
        tvAmountLabel.setTextSize(14);
        tvAmountLabel.setTextColor(getContext().getResources().getColor(R.color.primary));
        tvAmountLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvAmountLabel.setPadding(0, 0, 0, 8);

        android.widget.TextView tvPaymentLabel = new android.widget.TextView(getContext());
        tvPaymentLabel.setText("Payment Method: " + paymentMethod.toUpperCase());
        tvPaymentLabel.setTextSize(14);
        tvPaymentLabel.setTextColor(getContext().getResources().getColor(android.R.color.black));
        tvPaymentLabel.setPadding(0, 0, 0, 16);

        customerPaymentInfo.addView(tvCustLabel);
        customerPaymentInfo.addView(tvAmountLabel);
        customerPaymentInfo.addView(tvPaymentLabel);

        // Add separator before customer info
        android.view.View separator = new android.view.View(getContext());
        separator.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
        separator.setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray));
        customerPaymentInfo.addView(separator, 0);

        // Create and show dialog
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCloseReceipt.setOnClickListener(v -> {
            dialog.dismiss();
            processCheckout();
        });

        dialog.show();

        // Adjust dialog size based on content
        dialog.getWindow().setLayout(
            (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.95),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void populateReceiptItems(android.widget.LinearLayout itemsContainer) {
        itemsContainer.removeAllViews();

        // Use cart items passed from CartActivity
        if (cartItems != null) {
            for (com.example.pos_project.model.CartItem item : cartItems) {
            // Create item row
            android.widget.LinearLayout itemRow = new android.widget.LinearLayout(getContext());
            itemRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            itemRow.setPadding(8, 8, 8, 8);

            // Product name (left side)
            android.widget.TextView tvProductName = new android.widget.TextView(getContext());
            tvProductName.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            tvProductName.setText(item.getProductName());
            tvProductName.setTextSize(14);
            tvProductName.setTextColor(getContext().getResources().getColor(android.R.color.black));

            // Quantity and price (right side)
            android.widget.TextView tvItemDetails = new android.widget.TextView(getContext());
            tvItemDetails.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
            tvItemDetails.setText(String.format("%dx $%.2f = $%.2f",
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()));
            tvItemDetails.setTextSize(14);
            tvItemDetails.setTextColor(getContext().getResources().getColor(R.color.primary));
            tvItemDetails.setTypeface(null, android.graphics.Typeface.BOLD);

            // Add views to row
            itemRow.addView(tvProductName);
            itemRow.addView(tvItemDetails);

            // Add row to container
            itemsContainer.addView(itemRow);

            // Add separator line
            android.view.View separator = new android.view.View(getContext());
            separator.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
            separator.setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray));
            itemsContainer.addView(separator);
        }
        }
    }

    private void processCheckout() {
        // Dismiss dialog and notify listener
        dismiss();
        if (listener != null) {
            listener.onSaleComplete(customerName, totalAmount, amountPaid, paymentMethod);
        }
    }

    @Override
    public void onBackPressed() {
        // Don't allow back press - user must choose Continue or Skip
        // You can show a confirmation dialog here if needed
    }
}