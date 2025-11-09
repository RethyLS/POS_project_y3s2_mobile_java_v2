package com.example.pos_project.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.1.128:8000/api/"; // Using local network IP
    public static final String IMAGE_BASE_URL = "http://192.168.1.128:8000/api/storage/"; // For image serving
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY); // Enable full logging

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Create logging interceptor with maximum detail
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                System.out.println("API Log: " + message);
                android.util.Log.d("API_DEBUG", message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with interceptors
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        android.util.Log.d("API_DEBUG", "Making request to: " + chain.request().url());
                        return chain.proceed(chain.request());
                    })
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Method to update base URL if needed
    public static void updateBaseUrl(String newBaseUrl) {
        retrofit = null;
        apiService = null;
        // You'll need to rebuild the retrofit instance with new URL
    }
    
    // Method to fix URL hostname mismatch between Laravel backend and Android client
    public static String fixImageUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }
        
        // If it's a full URL with 0.0.0.0:8000, replace with 192.168.1.197:8000
        if (originalUrl.contains("0.0.0.0:8000")) {
            return originalUrl.replace("0.0.0.0:8000", "192.168.1.128:8000");
        }
        
        // If it's just a path, construct the full URL
        if (!originalUrl.startsWith("http")) {
            return IMAGE_BASE_URL + originalUrl;
        }
        
        return originalUrl;
    }
}