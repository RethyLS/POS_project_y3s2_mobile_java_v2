package com.example.pos_project.dto;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import com.example.pos_project.model.User;

public class LoginResponse {
    @SerializedName("user")
    @Expose
    private User user;
    
    @SerializedName("token")
    @Expose
    private String token;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(User user, String token) {
        this.user = user;
        this.token = token;
    }

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}