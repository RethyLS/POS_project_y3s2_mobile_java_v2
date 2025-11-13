package com.example.pos_project.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.example.pos_project.api.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;

public class NetworkUtils {
    
    // Interface for testing API connectivity
    interface TestService {
        @GET("api/test")
        Call<Void> testConnection();
    }
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    public static void testApiConnection(Context context) {
        if (!isNetworkAvailable(context)) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        
        TestService testService = ApiClient.getRetrofitInstance().create(TestService.class);
        Call<Void> call = testService.testConnection();
        
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // API connection successful - silently continue
                } else {
                    // API connection failed - silently continue  
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Network connection failed - silently continue
            }
        });
    }
}