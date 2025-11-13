package com.example.pos_project.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    @Expose(serialize = false, deserialize = false)
    private int id;
    
    @SerializedName("id")
    @Expose
    private int apiId;
    
    @SerializedName("image")
    @Expose
    private String image;
    
    @SerializedName("store_id")
    @Expose
    private int storeId;
    
    @SerializedName("category_id")
    @Expose
    private int categoryId;
    
    @SerializedName("name")
    @Expose
    private String name;
    
    @SerializedName("description")
    @Expose
    private String description;
    
    @SerializedName("sku")
    @Expose
    private String sku;
    
    @SerializedName("slug")
    @Expose
    private String slug;
    
    @SerializedName("barcode")
    @Expose
    private String barcode;
    
    @SerializedName("price")
    @Expose
    private double price;
    
    @SerializedName("cost_price")
    @Expose
    private double costPrice;
    
    @SerializedName("tax_rate")
    @Expose
    private double taxRate;
    
    @SerializedName("status")
    @Expose
    private String status;
    
    @SerializedName("quantity")
    @Expose
    private int quantity;
    
    @SerializedName("discount")
    @Expose
    private double discount;
    
    @SerializedName("quantity_alert")
    @Expose
    private int quantityAlert;
    
    @SerializedName("store")
    @Expose
    @Ignore // Ignore for Room database
    private Store store;
    
    @SerializedName("category")
    @Expose
    @Ignore // Ignore for Room database
    private Category category;
    
    // For local database storage
    private String categoryName;
    
    // Alternative field mapping for category name directly from API
    @SerializedName("category_name")
    @Expose
    private String apiCategoryName;
    private String storeName;
    private boolean isActive = true;

    // Constructors
    public Product() {}

    @Ignore
    public Product(String name, String description, double price, int quantity, String barcode) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.barcode = barcode;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getServerId() { return apiId; }
    public void setServerId(int serverId) { this.apiId = serverId; }
    
    // Room requires these methods for the apiId field
    public int getApiId() { return apiId; }
    public void setApiId(int apiId) { this.apiId = apiId; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public int getQuantityAlert() { return quantityAlert; }
    public void setQuantityAlert(int quantityAlert) { this.quantityAlert = quantityAlert; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { 
        this.category = category;
        // Also update the categoryName for local storage
        if (category != null) {
            this.categoryName = category.getName();
        }
    }

    public String getCategoryName() { 
        // Try multiple sources for category name
        if (categoryName != null && !categoryName.isEmpty()) {
            return categoryName;
        }
        if (apiCategoryName != null && !apiCategoryName.isEmpty()) {
            return apiCategoryName;
        }
        if (category != null && category.getName() != null && !category.getName().isEmpty()) {
            return category.getName();
        }
        return "Uncategorized"; // Fallback
    }
    
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getApiCategoryName() { return apiCategoryName; }
    public void setApiCategoryName(String apiCategoryName) { this.apiCategoryName = apiCategoryName; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}