package com.example.pos_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_project.R;
import com.example.pos_project.adapter.CategoryAdapter;
import com.example.pos_project.adapter.SaleProductAdapter;
import com.example.pos_project.activity.CartActivity;
import com.example.pos_project.auth.AuthManager;
import com.example.pos_project.database.POSDatabase;
import com.example.pos_project.model.CartItem;
import com.example.pos_project.model.Product;
import com.example.pos_project.repository.ProductRepository;
import com.example.pos_project.utils.CartManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SalesActivity extends AppCompatActivity implements 
        SaleProductAdapter.OnProductClickListener, CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvProductsSale, rvCategories;
    private EditText etSearchProducts;
    private Toolbar toolbar;
    private TextView tvCartBadge, tvCartBadgeToolbar, tvUsername;
    private androidx.appcompat.widget.AppCompatImageButton btnToolbarCart;
    private android.widget.ImageView ivProfileIcon;
    private ProgressBar progressLoading;
    
    // Bottom Action Panel Components
    private androidx.cardview.widget.CardView cartActionPanel;
    private TextView btnSaveCart, btnCheckout;
    private TextView tvSaveCartBadge, tvCheckoutBadge;

    private SaleProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    
    private List<Product> productList;
    private List<Product> filteredProductList;
    private List<String> categories;
    private boolean isLoadingProducts = false;
    
    private POSDatabase database;
    private ProductRepository productRepository;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_sales);
            initViews();
            setupUsername();
            setupToolbar();
            initDatabase();
            setupRecyclerViews();
            setupClickListeners();
            setupSearch();
            loadProducts();
            updateCartBadge();
        } catch (Exception e) {
            Toast.makeText(this, "Error starting activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Remove back arrow for home screen
            // Title is now handled by custom views in toolbar
        }
        
        // Set status bar color to match toolbar (light gray)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.background, getTheme()));
            // Make status bar icons dark since we're using light background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() | 
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void initViews() {
        android.util.Log.d("SalesActivity", "Initializing views");
        rvProductsSale = findViewById(R.id.rv_products_sale);
        rvCategories = findViewById(R.id.rv_categories);
        etSearchProducts = findViewById(R.id.et_search_products);
        toolbar = findViewById(R.id.toolbar);
        tvCartBadgeToolbar = findViewById(R.id.tv_cart_badge_toolbar);
        btnToolbarCart = findViewById(R.id.btn_toolbar_cart);
        tvUsername = findViewById(R.id.tv_username);
    // tvToolbarUsername removed from layout
        ivProfileIcon = findViewById(R.id.iv_profile_icon);
        cartActionPanel = findViewById(R.id.cart_action_panel);
        btnSaveCart = findViewById(R.id.btn_save_cart);
        btnCheckout = findViewById(R.id.btn_checkout);
        tvSaveCartBadge = findViewById(R.id.tv_save_cart_badge);
        tvCheckoutBadge = findViewById(R.id.tv_checkout_badge);
        progressLoading = findViewById(R.id.progress_loading);
        ivProfileIcon.setOnClickListener(v -> showLogoutDialog());
        setupToolbar();
        // Check other views
        if (rvCategories == null || etSearchProducts == null || 
            toolbar == null || tvCartBadgeToolbar == null || 
            cartActionPanel == null || btnSaveCart == null || btnCheckout == null) {
            android.util.Log.e("SalesActivity", "Some views are null!");
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        android.util.Log.d("SalesActivity", "All views initialized successfully");
    }

    private void setupUsername() {
        AuthManager authManager = AuthManager.getInstance(this);
        String userName = authManager.getUserName();
        
        if (userName != null && !userName.isEmpty()) {
            tvUsername.setText(userName);
        } else {
            // Fallback to email if name is not available
            String userEmail = authManager.getUserEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                // Extract name from email (part before @)
                String displayName = userEmail.split("@")[0];
                tvUsername.setText(displayName);
            } else {
                tvUsername.setText("User");
            }
        }
    }

    private void initDatabase() {
        try {
            // Reset cart manager to ensure clean state
            CartManager.resetInstance();
            
            database = POSDatabase.getInstance(this);
            executor = Executors.newFixedThreadPool(4);
            productRepository = new ProductRepository(this);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing database: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void setupRecyclerViews() {
        try {
            android.util.Log.d("SalesActivity", "Setting up RecyclerViews");
            
            // Products RecyclerView - Grid Layout
            productList = new ArrayList<>();
            filteredProductList = new ArrayList<>();
            productAdapter = new SaleProductAdapter(filteredProductList, this);
            android.util.Log.d("SalesActivity", "Created new product adapter");
            
            if (rvProductsSale != null) {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 columns
                rvProductsSale.setLayoutManager(gridLayoutManager);
                rvProductsSale.setAdapter(productAdapter);
                android.util.Log.d("SalesActivity", "Set up products RecyclerView with grid layout");
            } else {
                android.util.Log.e("SalesActivity", "Products RecyclerView is null in setupRecyclerViews!");
            }
            
            // Categories RecyclerView - Horizontal
            categories = new ArrayList<>();
            categories.add("All");
            // Categories will be populated dynamically from actual products
            categoryAdapter = new CategoryAdapter(categories, this);
            
            if (rvCategories != null) {
                LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                rvCategories.setLayoutManager(categoryLayoutManager);
                rvCategories.setAdapter(categoryAdapter);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up RecyclerViews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        // Toolbar Cart Button Click Listener
        btnToolbarCart.setOnClickListener(v -> {
            Intent cartIntent = new Intent(this, CartActivity.class);
            startActivity(cartIntent);
        });
        
        // Bottom Action Panel Click Listeners
        btnSaveCart.setOnClickListener(v -> {
            saveCurrentTicket();
        });
        
        btnCheckout.setOnClickListener(v -> {
            Intent cartIntent = new Intent(this, CartActivity.class);
            startActivity(cartIntent);
        });
    }

    private void updateCartBadge() {
        try {
            if (tvCartBadgeToolbar == null) {
                return; // Views not initialized yet
            }
            
            int cartItemCount = CartManager.getInstance().getCartItemCount();
            if (cartItemCount > 0) {
                // Update toolbar badge
                tvCartBadgeToolbar.setVisibility(View.VISIBLE);
                tvCartBadgeToolbar.setText(String.valueOf(cartItemCount));
                
                // Show Bottom Action Panel with animation
                if (cartActionPanel != null) {
                    cartActionPanel.setVisibility(View.VISIBLE);
                    cartActionPanel.animate()
                        .alpha(1.0f)
                        .translationY(0)
                        .setDuration(300)
                        .start();
                    
                    // Update bottom panel badges
                    if (tvSaveCartBadge != null) {
                        tvSaveCartBadge.setVisibility(View.VISIBLE);
                        tvSaveCartBadge.setText("1"); // Always show 1 for save cart
                    }
                    
                    if (tvCheckoutBadge != null) {
                        tvCheckoutBadge.setVisibility(View.VISIBLE);
                        tvCheckoutBadge.setText(String.valueOf(cartItemCount));
                    }
                }
            } else {
                // Hide toolbar badge
                tvCartBadgeToolbar.setVisibility(View.GONE);
                
                // Hide Bottom Action Panel with animation
                if (cartActionPanel != null) {
                    cartActionPanel.animate()
                        .alpha(0.0f)
                        .translationY(cartActionPanel.getHeight())
                        .setDuration(300)
                        .withEndAction(() -> cartActionPanel.setVisibility(View.GONE))
                        .start();
                }
            }
        } catch (Exception e) {
            // Silent catch to prevent crashes during cart badge update
        }
    }

    private void saveCurrentTicket() {
        if (CartManager.getInstance().getCartItems().isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique ticket number
        String ticketNumber = "TKT-" + System.currentTimeMillis();
        
        // For now, just show a toast. In the future, you can implement ticket saving to database
        Toast.makeText(this, "Ticket " + ticketNumber + " saved with " + 
                       CartManager.getInstance().getCartItemCount() + " items", 
                       Toast.LENGTH_LONG).show();
        
        // Optionally clear the cart after saving
        // CartManager.getInstance().clearCart();
        // updateCartBadge();
    }

    private void setupSearch() {
        etSearchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProducts(String query) {
        filteredProductList.clear();
        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getCategoryName().toLowerCase().contains(query.toLowerCase()) ||
                    (product.getBarcode() != null && product.getBarcode().toLowerCase().contains(query.toLowerCase())) ||
                    (product.getSku() != null && product.getSku().toLowerCase().contains(query.toLowerCase()))) {
                    filteredProductList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void loadProducts() {
        android.util.Log.d("SalesActivity", "loadProducts() called");
        if (productRepository == null) {
            android.util.Log.e("SalesActivity", "Repository is null!");
            Toast.makeText(this, "Repository not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isLoadingProducts) {
            android.util.Log.d("SalesActivity", "Already loading products, skipping");
            return; // Already loading, don't start another request
        }
        
        isLoadingProducts = true;
        android.util.Log.d("SalesActivity", "Starting product load...");
        // Show loading progress bar if products list is empty
        if (productList.isEmpty()) {
            if (progressLoading != null) {
                progressLoading.setVisibility(View.VISIBLE);
            }
        }
        
        productRepository.getAllProducts(new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                runOnUiThread(() -> {
                    try {
                        android.util.Log.d("SalesActivity", "Products loaded successfully. Count: " + products.size());
                        isLoadingProducts = false;
                        
                        // Debug: Log all products and their categories with detailed info
                        for (int i = 0; i < products.size(); i++) {
                            Product product = products.get(i);
                            android.util.Log.d("SalesActivity", String.format("Product %d: '%s' | Category: '%s' | API ID: %d", 
                                i + 1, product.getName(), product.getCategoryName(), product.getApiId()));
                        }
                        
                        // Hide loading progress bar
                        if (progressLoading != null) {
                            progressLoading.setVisibility(View.GONE);
                        }
                        
                        productList.clear();
                        productList.addAll(products);
                        filteredProductList.clear();
                        filteredProductList.addAll(products);
                        if (productAdapter != null) {
                            android.util.Log.d("SalesActivity", "Notifying adapter of data change");
                            productAdapter.notifyDataSetChanged();
                        } else {
                            android.util.Log.e("SalesActivity", "Product adapter is null!");
                        }
                        
                        // Update category list based on loaded products
                        updateCategoriesFromProducts(products);
                        
                    } catch (Exception e) {
                        isLoadingProducts = false;
                        
                        // Hide loading progress bar on error
                        if (progressLoading != null) {
                            progressLoading.setVisibility(View.GONE);
                        }
                        
                        Toast.makeText(SalesActivity.this, "Error updating product list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isLoadingProducts = false;
                    
                    // Hide loading progress bar
                    if (progressLoading != null) {
                        progressLoading.setVisibility(View.GONE);
                    }
                    
                    android.util.Log.e("SalesActivity", "Error loading products: " + error);
                    Toast.makeText(SalesActivity.this, "Error loading products: " + error, Toast.LENGTH_SHORT).show();
                    
                    // Keep existing categories on error - don't clear them
                });
            }
        });
    }

    private void updateCategoriesFromProducts(List<Product> products) {
        try {
            // Create a new list to build categories from actual products
            List<String> newCategories = new ArrayList<>();
            newCategories.add("All");
            
            android.util.Log.d("SalesActivity", "Starting category update with " + products.size() + " products");
            
            // Add unique categories from products
            for (Product product : products) {
                String categoryName = product.getCategoryName();
                android.util.Log.d("SalesActivity", "Product '" + product.getName() + "' category: '" + categoryName + "'");
                
                if (categoryName != null && !categoryName.trim().isEmpty() && 
                    !categoryName.equals("null") && !newCategories.contains(categoryName)) {
                    newCategories.add(categoryName);
                    android.util.Log.d("SalesActivity", "Added category: '" + categoryName + "'");
                } else {
                    android.util.Log.w("SalesActivity", "Skipping invalid category: '" + categoryName + "' for product: " + product.getName());
                }
            }
            
            // Always update with actual categories from products
            categories.clear();
            categories.addAll(newCategories);
            
            android.util.Log.d("SalesActivity", "Total categories found: " + (categories.size() - 1) + " (excluding 'All')");
            android.util.Log.d("SalesActivity", "Final categories: " + categories.toString());
            
            // Update on UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (categoryAdapter != null) {
                        categoryAdapter.notifyDataSetChanged();
                        android.util.Log.d("SalesActivity", "Category adapter updated on UI thread");
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("SalesActivity", "Error updating categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onAddToCartClick(Product product) {
        try {
            // Add product to cart using CartManager
            // Use serverId if available, otherwise fall back to local id
            int productId = product.getServerId() > 0 ? product.getServerId() : product.getId();
            CartItem cartItem = new CartItem(
                productId,
                product.getName(),
                product.getImage(),
                product.getPrice(),
                1
            );
            
            CartManager.getInstance().addToCart(cartItem);
            
            // Update UI
            updateCartBadge();
            
            // Vibrate for feedback (optional)
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(50); // Short vibration
            }
        } catch (Exception e) {
            android.util.Log.e("SalesActivity", "Error adding to cart: " + e.getMessage());
            Toast.makeText(this, "Error adding item to cart", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCategoryClick(String category, int position) {
        // Filter products by selected category
        if ("All".equals(category)) {
            filteredProductList.clear();
            filteredProductList.addAll(productList);
        } else {
            filteredProductList.clear();
            for (Product product : productList) {
                if (category.equalsIgnoreCase(product.getCategoryName())) {
                    filteredProductList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (executor != null) {
                executor.shutdown();
                executor = null;
            }
            // Clear adapters to prevent memory leaks
            if (productAdapter != null) {
                productAdapter = null;
            }
            if (categoryAdapter != null) {
                categoryAdapter = null;
            }
        } catch (Exception e) {
            // Silent catch to prevent crashes during cleanup
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save any necessary state here if needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge(); // Update cart badge when returning to this activity
        
        // Only load products if list is empty (first time) or if explicitly needed
        if (database != null && executor != null && productList.isEmpty()) {
            loadProducts();
        }
    }
    
    private void logout() {
        AuthManager authManager = AuthManager.getInstance(this);
        authManager.logout();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme);
        androidx.appcompat.app.AlertDialog dialog = builder
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (d, which) -> logout())
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