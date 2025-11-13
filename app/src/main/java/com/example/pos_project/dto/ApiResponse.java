package com.example.pos_project.dto;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import java.util.List;

public class ApiResponse<T> {
    @SerializedName("success")
    @Expose
    private boolean success;
    
    @SerializedName("message")
    @Expose
    private String message;
    
    @SerializedName("data")
    @Expose
    private T data;
    
    @SerializedName("meta")
    @Expose
    private PaginationMeta meta;

    // Constructors
    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public PaginationMeta getMeta() { return meta; }
    public void setMeta(PaginationMeta meta) { this.meta = meta; }
}