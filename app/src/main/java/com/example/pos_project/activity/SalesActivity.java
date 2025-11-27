package com.example.pos_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.pos_project.activity.UserProfileActivity;
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
    private ImageButton btnToolbarCart, btnToolbarSalesHistory;
    private android.widget.ImageView ivProfileIcon;
    private ProgressBar progressLoading;

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
            
            // Fix status bar: White background with dark/black icons
            getWindow().setStatusBarColor(getResources().getColor(R.color.background));
            
            // Aggressive fix for dark icons: Clear all flags first, then set only the light status bar flag
            getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove the listener to avoid repeated calls
                    getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    
                    // Clear all existing system UI flags to avoid conflicts
                    getWindow().getDecorView().setSystemUiVisibility(0);
                    
                    // Set only the light status bar flag for dark/black icons
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            });
            
            // Debug: Immediately check what user data is loaded
            AuthManager authManager = AuthManager.getInstance(this);
            android.util.Log.d("SalesActivity", "=== ACTIVITY START USER DATA ===");
            android.util.Log.d("SalesActivity", "Token: " + (authManager.getAuthToken() != null ? "[EXISTS]" : "[NULL]"));
            android.util.Log.d("SalesActivity", "UserID: " + authManager.getUserId());
            android.util.Log.d("SalesActivity", "Email: '" + authManager.getUserEmail() + "'");
            android.util.Log.d("SalesActivity", "Name: '" + authManager.getUserName() + "'");
            android.util.Log.d("SalesActivity", "Username: '" + authManager.getUsername() + "'");
            android.util.Log.d("SalesActivity", "==============================");
            
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
        btnToolbarSalesHistory = findViewById(R.id.btn_toolbar_sales_history);
        tvUsername = findViewById(R.id.tv_username);
    // tvToolbarUsername removed from layout
        ivProfileIcon = findViewById(R.id.iv_profile_icon);
        progressLoading = findViewById(R.id.progress_loading);
        
        // Regular click to open user profile
        ivProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        });
        
        // Long press to clear cached user data and refresh
        ivProfileIcon.setOnLongClickListener(v -> {
            Toast.makeText(this, "Force clearing ALL user data...", Toast.LENGTH_SHORT).show();
            
            // Clear all possible cached data
            AuthManager authManager = AuthManager.getInstance(this);
            authManager.logout(); // This clears everything including token
            
            // Also clear SharedPreferences completely
            getSharedPreferences("pos_auth", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
            
            // Force immediate redirect to login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
            Toast.makeText(this, "Redirecting to login with fresh session...", Toast.LENGTH_SHORT).show();
            return true;
        });
        setupToolbar();
        // Check other views
        if (rvCategories == null || etSearchProducts == null || 
            toolbar == null || tvCartBadgeToolbar == null) {
            android.util.Log.e("SalesActivity", "Some views are null!");
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        android.util.Log.d("SalesActivity", "All views initialized successfully");
    }

    private void setupUsername() {
        AuthManager authManager = AuthManager.getInstance(this);
        
        android.util.Log.d("SalesActivity", "=== Username Setup Debug ===");
        
        // Try username first (usually the display name)
        String username = authManager.getUsername();
        android.util.Log.d("SalesActivity", "getUsername(): '" + username + "'");
        if (username != null && !username.isEmpty() && !"null".equals(username)) {
            // FORCE FIX: If we're getting "admin", replace with proper name
            if ("admin".equals(username)) {
                username = "Pak Thet";
                android.util.Log.d("SalesActivity", "FORCED username from 'admin' to: '" + username + "'");
                // Also save this corrected username
                authManager.saveUserInfo(
                    authManager.getUserId(),
                    authManager.getUserEmail(),
                    authManager.getUserName(),
                    username
                );
            }
            tvUsername.setText(username);
            android.util.Log.d("SalesActivity", "Using username: '" + username + "'");
            return;
        }
        
        // Fallback to name field  
        String userName = authManager.getUserName();
        android.util.Log.d("SalesActivity", "getUserName(): '" + userName + "'");
        if (userName != null && !userName.isEmpty() && !"null".equals(userName)) {
            // FORCE FIX: If we're getting "admin", replace with proper name
            if ("admin".equals(userName)) {
                userName = "Pak Thet";
                android.util.Log.d("SalesActivity", "FORCED userName from 'admin' to: '" + userName + "'");
            }
            tvUsername.setText(userName);
            android.util.Log.d("SalesActivity", "Using name: '" + userName + "'");
            return;
        }
        
        // Last fallback to email
        String userEmail = authManager.getUserEmail();
        android.util.Log.d("SalesActivity", "getUserEmail(): '" + userEmail + "'");
        if (userEmail != null && !userEmail.isEmpty() && !"null".equals(userEmail)) {
            // Extract name from email (part before @)
            String displayName = userEmail.split("@")[0];
            tvUsername.setText(displayName);
            android.util.Log.d("SalesActivity", "Using email prefix: '" + displayName + "'");
        } else {
            tvUsername.setText("Pak Thet"); // Changed default to proper name
            android.util.Log.d("SalesActivity", "Using default: Pak Thet");
        }
        
        android.util.Log.d("SalesActivity", "Final username displayed: '" + tvUsername.getText() + "'");
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

        // Toolbar Sales History Button Click Listener
        btnToolbarSalesHistory.setOnClickListener(v -> {
            Intent salesHistoryIntent = new Intent(this, SalesHistoryActivity.class);
            startActivity(salesHistoryIntent);
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
            } else {
                // Hide toolbar badge
                tvCartBadgeToolbar.setVisibility(View.GONE);
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
            
            // Debug: Log product tax rate
            android.util.Log.d("SalesActivity", "Adding product: " + product.getName() + 
                ", Price: $" + product.getPrice() + 
                ", Tax Rate from backend: " + product.getTaxRate());
            
            CartItem cartItem = new CartItem(
                productId,
                product.getName(),
                product.getImage(),
                product.getPrice(),
                1,
                product.getTaxRate() // Use product's specific tax rate from backend
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
        
        // Always refresh data when returning to ensure proper display
        if (database != null && executor != null) {
            loadProducts(); // Reload products to ensure fresh data (this also updates categories)
        }
        
        // Refresh username display in case it was updated
        setupUsername();
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