package com.example.pos_project.repository;

import android.content.Context;
import android.widget.Toast;
import com.example.pos_project.api.ApiClient;
import com.example.pos_project.auth.AuthManager;
import com.example.pos_project.dto.ApiResponse;
import com.example.pos_project.model.Product;
import com.example.pos_project.database.POSDatabase;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private POSDatabase localDatabase;
    private Context context;
    private AuthManager authManager;

    public ProductRepository(Context context) {
        this.context = context;
        this.localDatabase = POSDatabase.getInstance(context);
        this.authManager = AuthManager.getInstance(context);
        // Don't initialize sample data automatically
    }
    
    // Only use sample data when API fails
    private void initializeSampleData() {
        new Thread(() -> {
            try {
                // Check if we already have products
                List<Product> existingProducts = localDatabase.productDao().getAllActiveProducts();
                if (existingProducts == null || existingProducts.isEmpty()) {
                    android.util.Log.d("ProductRepo", "No products in database, adding samples as fallback");
                    // Add sample products for testing
                    List<Product> sampleProducts = new ArrayList<>();
                    
                    Product product1 = new Product();
                    product1.setName("Sample Coffee");
                    product1.setPrice(4.50);
                    product1.setQuantity(10);
                    product1.setCategoryName("Beverages");
                    product1.setDescription("Delicious coffee");
                    product1.setActive(true);
                    
                    Product product2 = new Product();
                    product2.setName("Sample Sandwich");
                    product2.setPrice(8.99);
                    product2.setQuantity(5);
                    product2.setCategoryName("Food");
                    product2.setDescription("Fresh sandwich");
                    product2.setActive(true);
                    
                    Product product3 = new Product();
                    product3.setName("Sample Juice");
                    product3.setPrice(3.25);
                    product3.setQuantity(15);
                    product3.setCategoryName("Beverages");
                    product3.setDescription("Fresh fruit juice");
                    product3.setActive(true);
                    
                    sampleProducts.add(product1);
                    sampleProducts.add(product2);
                    sampleProducts.add(product3);
                    
                    localDatabase.productDao().insertAll(sampleProducts.toArray(new Product[0]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public interface ProductCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface SingleProductCallback {
        void onSuccess(Product product);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Fetch products from API and cache locally
    public void getAllProducts(ProductCallback callback) {
        android.util.Log.d("ProductRepo", "Starting getAllProducts request");
        
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            callback.onError("Not authenticated. Please login first.");
            return;
        }
        
        Call<ApiResponse<List<Product>>> call = ApiClient.getApiService().getAllProducts(authHeader);
        
        call.enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Product>>> call, Response<ApiResponse<List<Product>>> response) {
                android.util.Log.d("ProductRepo", "Got response. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Product>> apiResponse = response.body();
                    android.util.Log.d("ProductRepo", "Success: " + apiResponse.isSuccess() + ", Message: " + apiResponse.getMessage());
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Product> products = apiResponse.getData();
                        android.util.Log.d("ProductRepo", "Products received: " + products.size());
                        
                        // Log image URLs for debugging
                        for (Product product : products) {
                            android.util.Log.d("ProductRepo", "Product: " + product.getName() + ", Image: " + product.getImage());
                        }
                        
                        // Process products to extract store and category names for local storage
                        for (Product product : products) {
                            // serverId is already set correctly by Gson deserialization (apiId maps to "id")
                            
                            if (product.getCategory() != null) {
                                product.setCategoryName(product.getCategory().getName());
                            }
                            if (product.getStore() != null) {
                                product.setStoreName(product.getStore().getName());
                            }
                        }
                        
                        // Cache products locally in background
                        new Thread(() -> {
                            try {
                                // Clear old data and insert new
                                localDatabase.productDao().deleteAll();
                                localDatabase.productDao().insertAll(products.toArray(new Product[0]));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                        
                        callback.onSuccess(products);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    // Fallback to local data
                    getLocalProducts(callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Product>>> call, Throwable t) {
                // Network error, fallback to local data
                getLocalProducts(callback);
                Toast.makeText(context, "Using offline data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get products from local database
    private void getLocalProducts(ProductCallback callback) {
        new Thread(() -> {
            try {
                List<Product> products = localDatabase.productDao().getAllActiveProducts();
                if (products == null) {
                    products = new ArrayList<>();
                }
                
                // If no local data available, provide helpful message
                if (products.isEmpty()) {
                    callback.onError("No products found. Please check your internet connection and try again.");
                } else {
                    callback.onSuccess(products);
                }
            } catch (Exception e) {
                callback.onError("Error loading local data: " + e.getMessage());
            }
        }).start();
    }

    // Create product via API
    public void createProduct(Product product, SingleProductCallback callback) {
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            callback.onError("Not authenticated. Please login first.");
            return;
        }
        
        Call<ApiResponse<Product>> call = ApiClient.getApiService().createProduct(authHeader, product);
        
        call.enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Product> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Product createdProduct = apiResponse.getData();
                        
                        // Save to local database
                        new Thread(() -> {
                            localDatabase.productDao().insert(createdProduct);
                        }).start();
                        
                        callback.onSuccess(createdProduct);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to create product");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Update product via API
    public void updateProduct(Product product, SingleProductCallback callback) {
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            callback.onError("Not authenticated. Please login first.");
            return;
        }
        
        Call<ApiResponse<Product>> call = ApiClient.getApiService().updateProduct(authHeader, product.getId(), product);
        
        call.enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(Call<ApiResponse<Product>> call, Response<ApiResponse<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Product> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Product updatedProduct = apiResponse.getData();
                        
                        // Update local database
                        new Thread(() -> {
                            localDatabase.productDao().update(updatedProduct);
                        }).start();
                        
                        callback.onSuccess(updatedProduct);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to update product");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Product>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Delete product via API
    public void deleteProduct(int productId, SimpleCallback callback) {
        String authHeader = authManager.getAuthHeader();
        if (authHeader == null) {
            callback.onError("Not authenticated. Please login first.");
            return;
        }
        
        Call<ApiResponse<String>> call = ApiClient.getApiService().deleteProduct(authHeader, productId);
        
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // Delete from local database
                        new Thread(() -> {
                            localDatabase.productDao().deleteById(productId);
                        }).start();
                        
                        callback.onSuccess("Product deleted successfully");
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to delete product");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}