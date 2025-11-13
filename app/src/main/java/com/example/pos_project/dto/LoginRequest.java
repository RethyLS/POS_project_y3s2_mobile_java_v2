package com.example.pos_project.dto;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class LoginRequest {
    @SerializedName("email")
    @Expose
    private String email;
    
    @SerializedName("password")
    @Expose
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}