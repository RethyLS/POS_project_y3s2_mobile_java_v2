package com.example.pos_project.api;

import com.example.pos_project.dto.ApiResponse;
import com.example.pos_project.dto.LoginRequest;
import com.example.pos_project.dto.LoginResponse;
import com.example.pos_project.dto.SaleRequest;
import com.example.pos_project.dto.SaleResponse;
import com.example.pos_project.model.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    
    // Authentication endpoints
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest loginRequest);
    
    @POST("auth/logout")
    Call<ApiResponse<String>> logout(@Header("Authorization") String token);
    
    // Product endpoints
    @GET("products")
    Call<ApiResponse<List<Product>>> getAllProducts(@Header("Authorization") String token);
    
    @GET("products/{id}")
    Call<ApiResponse<Product>> getProduct(@Header("Authorization") String token, @Path("id") int id);
    
    @POST("products")
    Call<ApiResponse<Product>> createProduct(@Header("Authorization") String token, @Body Product product);
    
    @PUT("products/{id}")
    Call<ApiResponse<Product>> updateProduct(@Header("Authorization") String token, @Path("id") int id, @Body Product product);
    
    @DELETE("products/{id}")
    Call<ApiResponse<String>> deleteProduct(@Header("Authorization") String token, @Path("id") int id);
    
    // Category endpoints
    @GET("categories")
    Call<ApiResponse<List<com.example.pos_project.model.Category>>> getAllCategories(@Header("Authorization") String token);
    
    @GET("categories/{id}")
    Call<ApiResponse<com.example.pos_project.model.Category>> getCategory(@Header("Authorization") String token, @Path("id") int id);
    
    // Store endpoints
    @GET("stores")
    Call<ApiResponse<List<com.example.pos_project.model.Store>>> getAllStores(@Header("Authorization") String token);
    
    @GET("stores/{id}")
    Call<ApiResponse<com.example.pos_project.model.Store>> getStore(@Header("Authorization") String token, @Path("id") int id);
    
    // Sale endpoints
    @GET("sales?per_page=1000&with=")
    Call<ApiResponse<com.example.pos_project.dto.PaginatedData<com.example.pos_project.model.Sale>>> getAllSales(@Header("Authorization") String token);
    
    @POST("sales")
    Call<SaleResponse> createSale(@Header("Authorization") String token, @Body SaleRequest saleRequest);
}