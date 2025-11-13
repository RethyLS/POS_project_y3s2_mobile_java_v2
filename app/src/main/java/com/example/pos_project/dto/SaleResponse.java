package com.example.pos_project.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.example.pos_project.model.Sale;

public class SaleResponse {
    @Expose
    @SerializedName("success")
    private boolean success;
    
    @Expose
    @SerializedName("message")
    private String message;
    
    @Expose
    @SerializedName("data")
    private Sale data;

    public SaleResponse() {}

    public SaleResponse(boolean success, String message, Sale data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Sale getData() { return data; }
    public void setData(Sale data) { this.data = data; }
}