package com.example.pos_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private TextView tvCartBadge, tvCartBadgeToolbar;
    private androidx.appcompat.widget.AppCompatImageButton btnToolbarCart;
    private android.widget.ImageView ivProfileIcon;
    
    // Bottom Action Panel Components
    private androidx.cardview.widget.CardView cartActionPanel;
    private Button btnSaveCart, btnCheckout;
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
    // tvToolbarUsername removed from layout
        ivProfileIcon = findViewById(R.id.iv_profile_icon);
        cartActionPanel = findViewById(R.id.cart_action_panel);
        btnSaveCart = findViewById(R.id.btn_save_cart);
        btnCheckout = findViewById(R.id.btn_checkout);
        tvSaveCartBadge = findViewById(R.id.tv_save_cart_badge);
        tvCheckoutBadge = findViewById(R.id.tv_checkout_badge);
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

    private void initDatabase() {
        try {
            // Reset cart manager to ensure clean state
            CartManager.resetInstance();
            
            database = POSDatabase.getInstance(this);
            // Clear any old data from the database
            executor = Executors.newFixedThreadPool(4);
            executor.execute(() -> {
                database.productDao().deleteAll(); // Clear old data
            });
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
            categories.add("Food");
            categories.add("Drinks");
            categories.add("Electronics");
            categories.add("Clothing");
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
        // Only show loading toast if products list is empty
        if (productList.isEmpty()) {
            Toast.makeText(this, "Loading products...", Toast.LENGTH_SHORT).show();
        }
        
        productRepository.getAllProducts(new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                runOnUiThread(() -> {
                    try {
                        android.util.Log.d("SalesActivity", "Products loaded successfully. Count: " + products.size());
                        isLoadingProducts = false;
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
                        Toast.makeText(SalesActivity.this, "Error updating product list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isLoadingProducts = false;
                    Toast.makeText(SalesActivity.this, "Error loading products: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateCategoriesFromProducts(List<Product> products) {
        try {
            // Clear existing categories except "All"
            categories.clear();
            categories.add("All");
            
            // Add unique categories from products
            for (Product product : products) {
                String categoryName = product.getCategoryName();
                if (categoryName != null && !categoryName.isEmpty() && !categories.contains(categoryName)) {
                    categories.add(categoryName);
                }
            }
            
            if (categoryAdapter != null) {
                categoryAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddToCartClick(Product product) {
        try {
            // Add product to cart using CartManager
            CartItem cartItem = new CartItem(
                product.getId(),
                product.getName(),
                product.getImage(),
                product.getPrice(),
                1
            );
            
            CartManager.getInstance().addToCart(cartItem);
            
            // Update UI
            updateCartBadge();
            
            // Show confirmation with item count
            int itemCount = CartManager.getInstance().getCartItemCount();
            Toast.makeText(this, 
                product.getName() + " added to cart (Cart Items: " + itemCount + ")", 
                Toast.LENGTH_SHORT).show();
            
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
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> logout())
            .setNegativeButton("Cancel", null)
            .show();
    }
}