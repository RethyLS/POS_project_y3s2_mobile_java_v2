package com.example.pos_project.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos_project.R;
import com.example.pos_project.adapter.SalesHistoryAdapter;
import com.example.pos_project.api.ApiClient;
import com.example.pos_project.api.ApiService;
import com.example.pos_project.auth.AuthManager;
import com.example.pos_project.dto.ApiResponse;
import com.example.pos_project.dto.PaginatedData;
import com.example.pos_project.model.Sale;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalesHistoryActivity extends AppCompatActivity {

    private RecyclerView rvSalesHistory;
    private SalesHistoryAdapter salesAdapter;
    private List<Sale> salesList;
    private ProgressBar progressLoading;
    private TextView tvEmptyState;

    private ApiService apiService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sales History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set status bar color to white with dark icons
        getWindow().setStatusBarColor(getResources().getColor(R.color.background));

        // Aggressive fix for dark icons: Clear all flags first, then set only the light status bar flag
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid repeated calls
                getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Clear all existing system UI flags to avoid conflicts
                getWindow().getDecorView().setSystemUiVisibility(0);

                // Set only the light status bar flag for dark/black icons
                getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        });

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        initViews();

        // Initialize API service
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        authManager = AuthManager.getInstance(this);

        // Load sales history
        loadSalesHistory();
    }

    private void initViews() {
        rvSalesHistory = findViewById(R.id.rv_sales_history);
        progressLoading = findViewById(R.id.progress_loading);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        // Setup RecyclerView
        rvSalesHistory.setLayoutManager(new LinearLayoutManager(this));
        salesList = new ArrayList<>();
        salesAdapter = new SalesHistoryAdapter(salesList);
        rvSalesHistory.setAdapter(salesAdapter);
    }

    private void loadSalesHistory() {
        progressLoading.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        String token = authManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<ApiResponse<PaginatedData<Sale>>> call = apiService.getAllSales("Bearer " + token);
        call.enqueue(new Callback<ApiResponse<PaginatedData<Sale>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PaginatedData<Sale>>> call,
                                 Response<ApiResponse<PaginatedData<Sale>>> response) {
                progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PaginatedData<Sale>> apiResponse = response.body();
                    android.util.Log.d("SalesHistory", "API Response success: " + apiResponse.isSuccess());
                    android.util.Log.d("SalesHistory", "API Response message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess()) {
                        PaginatedData<Sale> paginatedData = apiResponse.getData();
                        android.util.Log.d("SalesHistory", "PaginatedData: " + (paginatedData != null ? "not null" : "null"));
                        
                        if (paginatedData != null) {
                            List<Sale> sales = paginatedData.getData();
                            android.util.Log.d("SalesHistory", "Sales list: " + (sales != null ? sales.size() + " items" : "null"));
                            
                            if (sales != null && !sales.isEmpty()) {
                                android.util.Log.d("SalesHistory", "First sale transaction_id: " + sales.get(0).getTransactionId());
                                android.util.Log.d("SalesHistory", "First sale total_amount: " + sales.get(0).getTotalAmount());
                                android.util.Log.d("SalesHistory", "First sale created_at: " + sales.get(0).getCreatedAt());
                                
                                salesList.clear();
                                salesList.addAll(sales);
                                salesAdapter.notifyDataSetChanged();
                            } else {
                                android.util.Log.d("SalesHistory", "No sales data");
                                showEmptyState();
                            }
                        } else {
                            android.util.Log.d("SalesHistory", "PaginatedData is null");
                            showEmptyState();
                        }
                    } else {
                        Toast.makeText(SalesHistoryActivity.this,
                            "Failed to load sales: " + apiResponse.getMessage(),
                            Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(SalesHistoryActivity.this,
                        "Failed to load sales history", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PaginatedData<Sale>>> call, Throwable t) {
                progressLoading.setVisibility(View.GONE);
                Toast.makeText(SalesHistoryActivity.this,
                    "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(View.VISIBLE);
        rvSalesHistory.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Normal back animation
    }
}