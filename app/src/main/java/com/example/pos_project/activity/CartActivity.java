package com.example.pos_project.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_project.R;
import com.example.pos_project.adapter.CartAdapter;
import com.example.pos_project.database.POSDatabase;
import com.example.pos_project.model.CartItem;
import com.example.pos_project.model.Sale;
import com.example.pos_project.model.SaleItem;
import com.example.pos_project.utils.CartManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemClickListener {

    private Toolbar toolbar;
    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyCart;
    private TextView tvTotalAmount;
    private TextView btnClearCart, btnCheckout, btnContinueShopping;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    
    private POSDatabase database;
    private ExecutorService executor;
    
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        initDatabase();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadCartItems();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCartItems = findViewById(R.id.rv_cart_items);
        layoutEmptyCart = findViewById(R.id.layout_empty_cart);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnClearCart = findViewById(R.id.btn_clear_cart);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
    }

    private void initDatabase() {
        database = POSDatabase.getInstance(this);
        executor = Executors.newFixedThreadPool(4);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);  // Disable default title
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItems, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        btnClearCart.setOnClickListener(v -> clearCart());
        btnCheckout.setOnClickListener(v -> showCheckoutDialog());
        btnContinueShopping.setOnClickListener(v -> finish());
    }

    private void showCheckoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_checkout, null);
        EditText etCustomerName = dialogView.findViewById(R.id.et_customer_name);
        EditText etAmountPaid = dialogView.findViewById(R.id.et_amount_paid);
        TextView btnConfirmCheckout = dialogView.findViewById(R.id.btn_confirm_checkout);
        TextView btnPaymentCash = dialogView.findViewById(R.id.btn_payment_cash);
        TextView btnPaymentCard = dialogView.findViewById(R.id.btn_payment_card);
        TextView btnPaymentMobile = dialogView.findViewById(R.id.btn_payment_mobile);
        TextView btnPaymentCredit = dialogView.findViewById(R.id.btn_payment_credit);

        final String[] selectedPaymentMethod = {"cash"};

        View.OnClickListener paymentClickListener = v -> {
            // Reset all buttons to unselected state (white background with primary border)
            setPaymentButtonUnselected(btnPaymentCash);
            setPaymentButtonUnselected(btnPaymentCard);
            setPaymentButtonUnselected(btnPaymentMobile);
            setPaymentButtonUnselected(btnPaymentCredit);
            
            // Set selected button to active state (primary background, white text)
            if (v == btnPaymentCash) {
                selectedPaymentMethod[0] = "cash";
                setPaymentButtonSelected(btnPaymentCash);
            } else if (v == btnPaymentCard) {
                selectedPaymentMethod[0] = "card";
                setPaymentButtonSelected(btnPaymentCard);
            } else if (v == btnPaymentMobile) {
                selectedPaymentMethod[0] = "mobile";
                setPaymentButtonSelected(btnPaymentMobile);
            } else if (v == btnPaymentCredit) {
                selectedPaymentMethod[0] = "credit";
                setPaymentButtonSelected(btnPaymentCredit);
            }
        };
        btnPaymentCash.setOnClickListener(paymentClickListener);
        btnPaymentCard.setOnClickListener(paymentClickListener);
        btnPaymentMobile.setOnClickListener(paymentClickListener);
        btnPaymentCredit.setOnClickListener(paymentClickListener);

        // Set initial state: Cash selected, others unselected
        setPaymentButtonSelected(btnPaymentCash);
        setPaymentButtonUnselected(btnPaymentCard);
        setPaymentButtonUnselected(btnPaymentMobile);
        setPaymentButtonUnselected(btnPaymentCredit);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnConfirmCheckout.setOnClickListener(v -> {
            String customerName = etCustomerName.getText().toString().trim();
            String amountPaidStr = etAmountPaid.getText().toString().trim();
            String paymentMethod = selectedPaymentMethod[0];

            if (amountPaidStr.isEmpty()) {
                etAmountPaid.setError("Please enter amount paid");
                return;
            }
            double amountPaid;
            try {
                amountPaid = Double.parseDouble(amountPaidStr);
            } catch (NumberFormatException e) {
                etAmountPaid.setError("Invalid amount");
                return;
            }
            if (amountPaid < totalAmount) {
                etAmountPaid.setError("Amount paid is less than total");
                return;
            }
            dialog.dismiss();
            processCheckout(customerName, amountPaid, paymentMethod);
        });
        dialog.show();
    }

    private void loadCartItems() {
        // Get cart items from CartManager singleton
        List<CartItem> items = CartManager.getInstance().getCartItems();
        cartItems.clear();
        cartItems.addAll(items);
        cartAdapter.notifyDataSetChanged();
        
        updateUI();
        updateTotalAmount();
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            rvCartItems.setVisibility(View.GONE);
            layoutEmptyCart.setVisibility(View.VISIBLE);
            findViewById(R.id.card_checkout).setVisibility(View.GONE);
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
            findViewById(R.id.card_checkout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onIncreaseQuantity(CartItem item, int position) {
        item.setQuantity(item.getQuantity() + 1);
        cartAdapter.notifyItemChanged(position);
        updateTotalAmount();
        CartManager.getInstance().updateCartItem(item);
    }

    @Override
    public void onDecreaseQuantity(CartItem item, int position) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartAdapter.notifyItemChanged(position);
            updateTotalAmount();
            CartManager.getInstance().updateCartItem(item);
        }
    }

    @Override
    public void onRemoveItem(CartItem item, int position) {
        cartItems.remove(position);
        cartAdapter.notifyItemRemoved(position);
        updateTotalAmount();
        updateUI();
        CartManager.getInstance().removeCartItem(item);
    }

    private void clearCart() {
        if (!cartItems.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Cart")
                    .setMessage("Are you sure you want to clear all items from the cart?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        cartItems.clear();
                        cartAdapter.notifyDataSetChanged();
                        updateTotalAmount();
                        updateUI();
                        CartManager.getInstance().clearCart();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void updateTotalAmount() {
        totalAmount = 0.0;
        for (CartItem item : cartItems) {
            totalAmount += item.getTotalPrice();
        }
        tvTotalAmount.setText(String.format("$%.2f", totalAmount));
        updateCheckoutButton();
    }

    private void updateCheckoutButton() {
        boolean hasItems = cartItems != null && !cartItems.isEmpty();
        btnCheckout.setEnabled(hasItems);
    }

    private void processCheckout(String customerName, double amountPaid, String paymentMethod) {
        String saleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        Sale sale = new Sale(saleDate, totalAmount, amountPaid, paymentMethod, 1, customerName);
        
        executor.execute(() -> {
            try {
                // Insert sale
                long saleId = database.saleDao().insert(sale);
                
                // Insert sale items and update product quantities
                List<SaleItem> saleItems = new ArrayList<>();
                for (CartItem cartItem : cartItems) {
                    SaleItem saleItem = new SaleItem((int) saleId, cartItem.getProductId(), 
                            cartItem.getProductName(), cartItem.getUnitPrice(), cartItem.getQuantity());
                    saleItems.add(saleItem);
                    
                    // Reduce product quantity
                    database.productDao().reduceProductQuantity(cartItem.getProductId(), cartItem.getQuantity());
                }
                database.saleItemDao().insertAll(saleItems);
                
                runOnUiThread(() -> {
                    Toast.makeText(CartActivity.this, "Sale recorded successfully", Toast.LENGTH_SHORT).show();
                    showSaleCompleteDialog(totalAmount, amountPaid, paymentMethod);
                    clearCartAfterSale();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CartActivity.this, "Error processing sale: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showSaleCompleteDialog(double totalAmount, double amountPaid, String paymentMethod) {
        double change = amountPaid - totalAmount;
        
        // Create custom dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom, null);
        
        // Find views
        TextView titleText = dialogView.findViewById(R.id.dialog_title);
        TextView messageText = dialogView.findViewById(R.id.dialog_message);
        TextView cancelBtn = dialogView.findViewById(R.id.dialog_cancel_btn);
        TextView confirmBtn = dialogView.findViewById(R.id.dialog_confirm_btn);
        
        // Set content
        titleText.setText("Sale Complete");
        messageText.setText(String.format(Locale.getDefault(),
                "Payment Method: %s\nTotal: $%.2f\nPaid: $%.2f\nChange: $%.2f",
                paymentMethod.toUpperCase(), totalAmount, amountPaid, change));
        
        // Hide cancel button for this dialog
        cancelBtn.setVisibility(View.GONE);
        confirmBtn.setText("OK");
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        confirmBtn.setOnClickListener(v -> {
            dialog.dismiss();
            // Return to sales activity
            Intent intent = new Intent(this, com.example.pos_project.activity.SalesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        dialog.show();
    }

    private void showCheckoutSuccess(double change) {
        String message = String.format("Sale completed successfully!\n\nTotal: $%.2f\nChange: $%.2f", 
                totalAmount, change);
        
        new AlertDialog.Builder(this)
                .setTitle("Sale Complete")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Return to sales activity
                    Intent intent = new Intent(this, com.example.pos_project.activity.SalesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void clearCartAfterSale() {
        cartItems.clear();
        cartAdapter.notifyDataSetChanged();
        updateTotalAmount();
        updateUI();
        CartManager.getInstance().clearCart();
        // No need to clear dialog views here
    }

    private void updatePaymentButtonTextColor(androidx.cardview.widget.CardView cardView, int colorRes) {
        LinearLayout layout = (LinearLayout) cardView.getChildAt(0);
        for (int i = 0; i < layout.getChildCount(); i++) {
            if (layout.getChildAt(i) instanceof TextView) {
                TextView textView = (TextView) layout.getChildAt(i);
                textView.setTextColor(getResources().getColor(colorRes));
            }
        }
    }

    private void setPaymentButtonSelected(TextView button) {
        // Primary background with white text (current selected style)
        button.setBackgroundResource(R.drawable.button_primary_rounded);
        button.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void setPaymentButtonUnselected(TextView button) {
        // White background with primary border (same as Back button style)
        button.setBackgroundResource(R.drawable.button_white_border_primary);
        button.setTextColor(getResources().getColor(android.R.color.black));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}