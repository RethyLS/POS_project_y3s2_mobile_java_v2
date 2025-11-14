package com.example.pos_project.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.example.pos_project.api.ApiClient;
import com.example.pos_project.api.ApiService;
import com.example.pos_project.auth.AuthManager;
import com.example.pos_project.database.POSDatabase;
import com.example.pos_project.dto.SaleRequest;
import com.example.pos_project.dto.SaleItemRequest;
import com.example.pos_project.dto.SaleResponse;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemClickListener {

    private Toolbar toolbar;
    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyCart;
    private TextView tvSubtotal, tvTax, tvTotalAmount;
    private TextView btnClearCart, btnCheckout, btnContinueShopping;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    
    private POSDatabase database;
    private ExecutorService executor;
    private AuthManager authManager;
    
    // Loading indicator
    private FrameLayout loadingOverlay;
    
    // Tax calculations will use individual product tax rates from backend
    
    private double subtotalAmount = 0.0;
    private double taxAmount = 0.0;
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Set status bar color to white and make icons dark
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.background, getTheme()));
            // Make status bar icons dark since we're using light background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() |
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

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
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTax = findViewById(R.id.tv_tax);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnClearCart = findViewById(R.id.btn_clear_cart);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void initDatabase() {
        database = POSDatabase.getInstance(this);
        executor = Executors.newFixedThreadPool(4);
        authManager = AuthManager.getInstance(this);
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
        TextView tvTotalPrice = dialogView.findViewById(R.id.tv_total_price);
        TextView btnConfirmCheckout = dialogView.findViewById(R.id.btn_confirm_checkout);
        TextView btnPaymentCash = dialogView.findViewById(R.id.btn_payment_cash);
        TextView btnPaymentCard = dialogView.findViewById(R.id.btn_payment_card);
        TextView btnPaymentMobile = dialogView.findViewById(R.id.btn_payment_mobile);
        TextView btnPaymentCredit = dialogView.findViewById(R.id.btn_payment_credit);

        // Set total price
        tvTotalPrice.setText(String.format("Total: $%.2f", totalAmount));

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
            // Show Sale Complete Dialog
            SaleCompleteDialog saleCompleteDialog = new SaleCompleteDialog(CartActivity.this,
                customerName, totalAmount, amountPaid, paymentMethod,
                new ArrayList<>(cartItems), (custName, total, paid, method) -> {
                    // Process the checkout when dialog completes
                    processCheckout(custName, paid, method);
                });
            saleCompleteDialog.show();
        });
        dialog.show();
    }

    private void showReceiptDialog(String customerName, double amountPaid, String paymentMethod) {
        // Ensure calculations are up to date before showing receipt
        updateTotalAmount();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_receipt_checkout, null);

        // Get references to views
        LinearLayout itemsContainer = dialogView.findViewById(R.id.items_container);
        TextView tvReceiptSubtotal = dialogView.findViewById(R.id.tv_receipt_subtotal);
        TextView tvReceiptTax = dialogView.findViewById(R.id.tv_receipt_tax);
        TextView tvReceiptTotal = dialogView.findViewById(R.id.tv_receipt_total);
        TextView tvReceiptAmountPaid = dialogView.findViewById(R.id.tv_receipt_amount_paid);
        TextView tvReceiptChange = dialogView.findViewById(R.id.tv_receipt_change);
        TextView tvSeller = dialogView.findViewById(R.id.tv_receipt_seller);
        LinearLayout customerPaymentInfo = dialogView.findViewById(R.id.customer_payment_info);
        ImageView btnCloseReceipt = dialogView.findViewById(R.id.btn_close_receipt);

        // Set seller name (user login name)
        String sellerName = com.example.pos_project.auth.AuthManager.getInstance(this).getUserName();
        if (sellerName != null && !sellerName.isEmpty()) {
            // FORCE FIX: If we're getting "admin", replace with proper name
            if ("admin".equals(sellerName)) {
                sellerName = "Pak Thet";
            }
            tvSeller.setText(sellerName);
        } else {
            tvSeller.setText("Pak Thet"); // Changed default to proper name
        }

        // Debug: Log all financial values
        android.util.Log.d("CartActivity", "Receipt values - Subtotal: $" + subtotalAmount + 
            ", Tax: $" + taxAmount + ", Total: $" + totalAmount + ", Amount Paid: $" + amountPaid);

        // Set financial values
        if (tvReceiptSubtotal != null) {
            tvReceiptSubtotal.setText(String.format("$%.2f", subtotalAmount));
            android.util.Log.d("CartActivity", "Set subtotal: $" + String.format("%.2f", subtotalAmount));
        } else {
            android.util.Log.e("CartActivity", "tvReceiptSubtotal is null!");
        }
        
        if (tvReceiptTax != null) {
            tvReceiptTax.setText(String.format("$%.2f", taxAmount));
            android.util.Log.d("CartActivity", "Set tax: $" + String.format("%.2f", taxAmount));
        } else {
            android.util.Log.e("CartActivity", "tvReceiptTax is null!");
        }
        
        if (tvReceiptTotal != null) {
            tvReceiptTotal.setText(String.format("$%.2f", totalAmount));
            android.util.Log.d("CartActivity", "Set total: $" + String.format("%.2f", totalAmount));
        } else {
            android.util.Log.e("CartActivity", "tvReceiptTotal is null!");
        }
        
        if (tvReceiptAmountPaid != null) {
            tvReceiptAmountPaid.setText(String.format("$%.2f", amountPaid));
            android.util.Log.d("CartActivity", "Set amount paid: $" + String.format("%.2f", amountPaid));
        } else {
            android.util.Log.e("CartActivity", "tvReceiptAmountPaid is null!");
        }
        
        // Calculate and display change
        double change = amountPaid - totalAmount;
        if (tvReceiptChange != null) {
            tvReceiptChange.setText(String.format("$%.2f", change));
            android.util.Log.d("CartActivity", "Set change: $" + String.format("%.2f", change));
        } else {
            android.util.Log.e("CartActivity", "tvReceiptChange is null!");
        }

        // Populate items list
        populateReceiptItems(itemsContainer);

        // Add customer name and payment method info
        TextView tvCustLabel = new TextView(this);
        tvCustLabel.setText("Customer: " + customerName);
        tvCustLabel.setTextSize(14);
        tvCustLabel.setTextColor(getResources().getColor(android.R.color.black));
        tvCustLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCustLabel.setPadding(0, 0, 0, 8);

        TextView tvPaymentLabel = new TextView(this);
        tvPaymentLabel.setText("Payment Method: " + paymentMethod.toUpperCase());
        tvPaymentLabel.setTextSize(14);
        tvPaymentLabel.setTextColor(getResources().getColor(android.R.color.black));
        tvPaymentLabel.setPadding(0, 0, 0, 16);

        customerPaymentInfo.addView(tvCustLabel);
        customerPaymentInfo.addView(tvPaymentLabel);

        // Add separator before customer info
        View separator = new View(this);
        separator.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1));
        separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        customerPaymentInfo.addView(separator, 0);

        // Create and show dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCloseReceipt.setOnClickListener(v -> {
            // Close receipt dialog first
            dialog.dismiss();
            
            // Show loading indicator immediately
            android.util.Log.d("CartActivity", "About to show loading overlay");
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.VISIBLE);
                android.util.Log.d("CartActivity", "Loading overlay shown");
            } else {
                android.util.Log.e("CartActivity", "Loading overlay is null!");
            }
            
            // Process checkout with delay to show loading
            new android.os.Handler().postDelayed(() -> {
                android.util.Log.d("CartActivity", "Starting checkout process");
                processCheckout(customerName, amountPaid, paymentMethod);
            }, 2000); // Longer delay to clearly see loading
        });

        dialog.show();

        // Adjust dialog size based on content
        dialog.getWindow().setLayout(
            (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void populateReceiptItems(LinearLayout itemsContainer) {
        itemsContainer.removeAllViews();

        for (CartItem item : cartItems) {
            // Create item row
            LinearLayout itemRow = new LinearLayout(this);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setPadding(8, 8, 8, 8);

            // Product name (left side)
            TextView tvProductName = new TextView(this);
            tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            tvProductName.setText(item.getProductName());
            tvProductName.setTextSize(14);
            tvProductName.setTextColor(getResources().getColor(android.R.color.black));

            // Quantity and price (right side)
            TextView tvItemDetails = new TextView(this);
            tvItemDetails.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvItemDetails.setText(String.format("%dx $%.2f = $%.2f",
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()));
            tvItemDetails.setTextSize(14);
            tvItemDetails.setTextColor(getResources().getColor(android.R.color.black));
            tvItemDetails.setTypeface(null, android.graphics.Typeface.BOLD);

            // Add views to row
            itemRow.addView(tvProductName);
            itemRow.addView(tvItemDetails);

            // Add row to container
            itemsContainer.addView(itemRow);

            // Add separator line
            View separator = new View(this);
            separator.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
            separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            itemsContainer.addView(separator);
        }
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
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme);
            androidx.appcompat.app.AlertDialog dialog = builder
                    .setTitle("Clear Cart")
                    .setMessage("Are you sure you want to clear all items from the cart?")
                    .setPositiveButton("Clear", (dialogInterface, which) -> {
                        cartItems.clear();
                        cartAdapter.notifyDataSetChanged();
                        updateTotalAmount();
                        updateUI();
                        CartManager.getInstance().clearCart();
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            
            dialog.show();
            
            // Set dialog width to 80% of screen width
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                android.view.WindowManager.LayoutParams layoutParams = window.getAttributes();
                android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                layoutParams.width = (int) (displayMetrics.widthPixels * 0.8);
                window.setAttributes(layoutParams);
            }
        }
    }

    private void updateTotalAmount() {
        // Calculate subtotal (sum of all item prices without tax)
        subtotalAmount = 0.0;
        taxAmount = 0.0;
        
        for (CartItem item : cartItems) {
            double itemSubtotal = item.getTotalPrice();
            
            // Convert tax rate from percentage to decimal
            // If backend sends 16 (meaning 16%), convert to 0.16
            double taxRateDecimal = item.getTaxRate();
            if (taxRateDecimal > 1.0) {
                // Tax rate is in percentage format (e.g., 16), convert to decimal (0.16)
                taxRateDecimal = taxRateDecimal / 100.0;
            }
            
            double itemTax = itemSubtotal * taxRateDecimal;
            
            subtotalAmount += itemSubtotal;
            taxAmount += itemTax;
        }
        
        // Calculate total (subtotal + tax)
        totalAmount = subtotalAmount + taxAmount;
        
        // Update UI
        if (tvSubtotal != null) {
            tvSubtotal.setText(String.format("$%.2f", subtotalAmount));
        }
        if (tvTax != null) {
            tvTax.setText(String.format("$%.2f", taxAmount));
        }
        if (tvTotalAmount != null) {
            tvTotalAmount.setText(String.format("$%.2f", totalAmount));
        }
        
        updateCheckoutButton();
    }

    private void updateCheckoutButton() {
        boolean hasItems = cartItems != null && !cartItems.isEmpty();
        btnCheckout.setEnabled(hasItems);
    }

    private void processCheckout(String customerName, double amountPaid, String paymentMethod) {
        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first to process sales", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get auth token
        String token = authManager.getAuthToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Authentication token not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get cashier ID
        int cashierId = authManager.getUserId();

        // TEMPORARY FIX: Use a valid cashier ID if current one is invalid
        if (cashierId == -1 || cashierId == 0) {
            android.util.Log.w("CartActivity", "Invalid cashier ID: " + cashierId + ", using default ID 1");
            cashierId = 1; // Use admin user ID as fallback
        }

        // Convert payment method to API format
        String apiPaymentMethod = convertPaymentMethod(paymentMethod);

        // Calculate amounts following Next.js approach
        double subtotalAmount = 0.0;
        double taxAmount = 0.0;
        
        // Create sale items list
        List<SaleItemRequest> saleItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            // Calculate subtotal for this item
            double itemSubtotal = cartItem.getQuantity() * cartItem.getUnitPrice();
            subtotalAmount += itemSubtotal;
            
            // Calculate tax for this item (assuming tax rate is per item)
            double itemTaxRate = 0.0; // You may need to get this from product data
            double itemTax = itemSubtotal * (itemTaxRate / 100);
            taxAmount += itemTax;
            
            SaleItemRequest item = new SaleItemRequest(
                cartItem.getProductId(), // Use serverId if available, local ID otherwise
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                itemTaxRate,
                0.0  // discount amount
            );
            saleItems.add(item);
            android.util.Log.d("CartActivity", "Sale item: productId=" + cartItem.getProductId() +
                ", quantity=" + cartItem.getQuantity() + ", unitPrice=" + cartItem.getUnitPrice());
        }
        
        double discountAmount = 0.0; // No discount for now
        double totalAmountCalculated = subtotalAmount + taxAmount - discountAmount;

        android.util.Log.d("CartActivity", "Final sale request data:");
        android.util.Log.d("CartActivity", "Customer ID: null (walk-in)");
        android.util.Log.d("CartActivity", "Cashier ID: " + cashierId);
        android.util.Log.d("CartActivity", "Payment method: " + apiPaymentMethod);
        android.util.Log.d("CartActivity", "Items count: " + saleItems.size());
        android.util.Log.d("CartActivity", "Subtotal: " + subtotalAmount);
        android.util.Log.d("CartActivity", "Tax: " + taxAmount);
        android.util.Log.d("CartActivity", "Discount: " + discountAmount);
        android.util.Log.d("CartActivity", "Total: " + totalAmountCalculated);

        // Create sale request matching Next.js structure
        SaleRequest saleRequest = new SaleRequest(
            null, // customer_id - explicitly set to null for walk-in customers
            cashierId,
            apiPaymentMethod,
            saleItems,
            subtotalAmount,
            taxAmount,
            discountAmount,
            totalAmountCalculated,
            "Sale from Android POS"
        );

        // Log the complete sale request JSON with custom Gson that includes null values
        try {
            // Create Gson that serializes null values
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
            String saleRequestJson = gson.toJson(saleRequest);
            android.util.Log.d("CartActivity", "Sale Request JSON: " + saleRequestJson);
            android.util.Log.d("CartActivity", "Request Details:");
            android.util.Log.d("CartActivity", "- customer_id: " + saleRequest.getCustomerId());
            android.util.Log.d("CartActivity", "- cashier_id: " + saleRequest.getCashierId());
            android.util.Log.d("CartActivity", "- payment_method: " + saleRequest.getPaymentMethod());
            android.util.Log.d("CartActivity", "- items count: " + saleRequest.getItems().size());
            android.util.Log.d("CartActivity", "- subtotal_amount: " + saleRequest.getSubtotalAmount());
            android.util.Log.d("CartActivity", "- tax_amount: " + saleRequest.getTaxAmount());
            android.util.Log.d("CartActivity", "- discount_amount: " + saleRequest.getDiscountAmount());
            android.util.Log.d("CartActivity", "- total_amount: " + saleRequest.getTotalAmount());
            android.util.Log.d("CartActivity", "- notes: " + saleRequest.getNotes());
            
            // Log each item details
            for (int i = 0; i < saleRequest.getItems().size(); i++) {
                SaleItemRequest item = saleRequest.getItems().get(i);
                android.util.Log.d("CartActivity", "Item " + i + ": product_id=" + item.getProductId() +
                    ", quantity=" + item.getQuantity() + ", unit_price=" + item.getUnitPrice() +
                    ", tax_rate=" + item.getTaxRate() + ", discount_amount=" + item.getDiscountAmount());
            }
        } catch (Exception e) {
            android.util.Log.e("CartActivity", "Error logging sale request: " + e.getMessage());
        }

        // Call API
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<SaleResponse> call = apiService.createSale("Bearer " + token, saleRequest);

        call.enqueue(new Callback<SaleResponse>() {
            @Override
            public void onResponse(Call<SaleResponse> call, Response<SaleResponse> response) {
                android.util.Log.d("CartActivity", "API Response Code: " + response.code());
                android.util.Log.d("CartActivity", "API Response Message: " + response.message());
                android.util.Log.d("CartActivity", "Response is successful: " + response.isSuccessful());
                
                if (response.body() != null) {
                    android.util.Log.d("CartActivity", "Response body success: " + response.body().isSuccess());
                    android.util.Log.d("CartActivity", "Response body message: " + response.body().getMessage());
                } else {
                    android.util.Log.e("CartActivity", "Response body is null");
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // API call successful - also update local database for offline capability
                    updateLocalDatabase(customerName, amountPaid, paymentMethod);
                    runOnUiThread(() -> {
                        // Hide loading overlay
                        if (loadingOverlay != null) {
                            loadingOverlay.setVisibility(View.GONE);
                        }
                        
                        clearCartAfterSale();
                        Toast.makeText(CartActivity.this, "Sale completed successfully!", Toast.LENGTH_SHORT).show();
                        
                        Intent intent = new Intent(CartActivity.this, com.example.pos_project.activity.SalesActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        String errorMsg = "Sale failed";
                        String detailedError = "";
                        
                        // Enhanced error logging and parsing
                        android.util.Log.e("CartActivity", "API Error - Response Code: " + response.code());
                        android.util.Log.e("CartActivity", "API Error - Response Message: " + response.message());

                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg = "Sale failed: " + response.body().getMessage();
                            android.util.Log.e("CartActivity", "API Error - Response Body Message: " + response.body().getMessage());
                        } else if (response.errorBody() != null) {
                            // Try to get error from response body
                            try {
                                String errorBody = response.errorBody().string();
                                android.util.Log.e("CartActivity", "API Error Response Body: " + errorBody);

                                // Try to parse as JSON to get detailed validation errors
                                try {
                                    com.google.gson.Gson gson = new com.google.gson.Gson();
                                    com.google.gson.JsonObject errorJson = gson.fromJson(errorBody, com.google.gson.JsonObject.class);
                                    
                                    if (errorJson.has("message")) {
                                        String apiMessage = errorJson.get("message").getAsString();
                                        errorMsg = "API Error: " + apiMessage;
                                        android.util.Log.e("CartActivity", "API Error Message: " + apiMessage);
                                    }
                                    
                                    if (errorJson.has("errors")) {
                                        com.google.gson.JsonObject errors = errorJson.getAsJsonObject("errors");
                                        StringBuilder errorDetails = new StringBuilder();
                                        
                                        for (String key : errors.keySet()) {
                                            if (errors.get(key).isJsonArray()) {
                                                com.google.gson.JsonArray errorArray = errors.getAsJsonArray(key);
                                                for (int i = 0; i < errorArray.size(); i++) {
                                                    errorDetails.append(key).append(": ").append(errorArray.get(i).getAsString()).append("\n");
                                                }
                                            }
                                        }
                                        
                                        if (errorDetails.length() > 0) {
                                            detailedError = "Validation errors:\n" + errorDetails.toString();
                                            android.util.Log.e("CartActivity", "Validation Errors: " + errorDetails.toString());
                                        }
                                    }
                                } catch (com.google.gson.JsonSyntaxException jsonE) {
                                    android.util.Log.e("CartActivity", "Failed to parse error as JSON: " + jsonE.getMessage());
                                    // Fall back to plain text error
                                    if (errorBody.contains("validation") || errorBody.contains("required")) {
                                        errorMsg = "Validation Error - Required fields missing";
                                        detailedError = "Please check: cashier ID, payment method, products, and amounts";
                                    } else {
                                        errorMsg = "Sale failed: " + errorBody;
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("CartActivity", "Error reading error response: " + e.getMessage());
                                errorMsg = "Sale failed (HTTP " + response.code() + ")";
                            }
                        } else {
                            errorMsg = "Sale failed (HTTP " + response.code() + ")";
                        }

                        final String finalErrorMsg = errorMsg;
                        final String finalDetailedError = detailedError;

                        // Show error with option to save locally
                        new AlertDialog.Builder(CartActivity.this)
                            .setTitle("Sale Failed")
                            .setMessage(finalErrorMsg + (finalDetailedError.isEmpty() ? "\n\nWould you like to save this sale locally only?" : "\n\n" + finalDetailedError + "\n\nWould you like to save this sale locally only?"))
                            .setPositiveButton("Save Locally", (dialog, which) -> {
                                updateLocalDatabase(customerName, amountPaid, paymentMethod);
                                clearCartAfterSale();
                                Toast.makeText(CartActivity.this, "Sale saved locally", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CartActivity.this, com.example.pos_project.activity.SalesActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton("Retry", (dialog, which) -> {
                                // Retry the checkout
                                processCheckout(customerName, amountPaid, paymentMethod);
                            })
                            .setNeutralButton("Cancel", null)
                            .show();
                    });
                }
            }

            @Override
            public void onFailure(Call<SaleResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    // Show error with option to save locally
                    new AlertDialog.Builder(CartActivity.this)
                        .setTitle("Network Error")
                        .setMessage("Failed to connect to server: " + t.getMessage() + "\n\nWould you like to save this sale locally only?")
                        .setPositiveButton("Save Locally", (dialog, which) -> {
                            updateLocalDatabase(customerName, amountPaid, paymentMethod);
                            clearCartAfterSale();
                            Toast.makeText(CartActivity.this, "Sale saved locally", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CartActivity.this, com.example.pos_project.activity.SalesActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Retry", (dialog, which) -> {
                            // Retry the checkout
                            processCheckout(customerName, amountPaid, paymentMethod);
                        })
                        .setNeutralButton("Cancel", null)
                        .show();
                });
            }
        });
    }

    private void updateLocalDatabase(String customerName, double amountPaid, String paymentMethod) {
        executor.execute(() -> {
            try {
                String saleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                Sale sale = new Sale(saleDate, totalAmount, amountPaid, paymentMethod, 1, customerName);

                // Insert sale
                long saleId = database.saleDao().insert(sale);

                // Insert sale items and update product quantities
                List<SaleItem> saleItems = new ArrayList<>();
                for (CartItem cartItem : cartItems) {
                    SaleItem saleItem = new SaleItem((int) saleId, cartItem.getProductId(),
                            cartItem.getProductName(), cartItem.getUnitPrice(), cartItem.getQuantity());
                    saleItems.add(saleItem);

                    // Reduce product quantity locally
                    database.productDao().reduceProductQuantity(cartItem.getProductId(), cartItem.getQuantity());
                }
                database.saleItemDao().insertAll(saleItems);

            } catch (Exception e) {
                android.util.Log.e("CartActivity", "Error updating local database: " + e.getMessage(), e);
            }
        });
    }

    private String convertPaymentMethod(String paymentMethod) {
        switch (paymentMethod.toLowerCase()) {
            case "cash":
                return "cash";
            case "card":
                return "card";
            case "credit":
                return "credit";
            default:
                return "cash"; // default fallback
        }
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