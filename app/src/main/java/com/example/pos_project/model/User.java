package com.example.pos_project.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    @Expose
    private int id;
    
    @SerializedName("name")
    @Expose
    private String name;
    
    @SerializedName("email")
    @Expose
    private String email;
    
    @SerializedName("username")
    @Expose
    private String username;
    
    private String password; // Not serialized for security
    
    @SerializedName("role")
    @Expose
    private String role; // "admin" or "cashier"
    
    private String fullName; // Local field, mapped from name
    
    @SerializedName("email_verified_at")
    @Expose
    private String emailVerifiedAt;
    
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    
    private boolean isActive = true; // Local field

    // Constructors
    public User() {}

    @Ignore
    public User(String username, String password, String role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.name = fullName;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        // Also update fullName for local compatibility
        if (this.fullName == null || this.fullName.isEmpty()) {
            this.fullName = name;
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName != null ? fullName : name; }
    public void setFullName(String fullName) { 
        this.fullName = fullName;
        // Also update name for API compatibility
        if (this.name == null || this.name.isEmpty()) {
            this.name = fullName;
        }
    }

    public String getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(String emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}