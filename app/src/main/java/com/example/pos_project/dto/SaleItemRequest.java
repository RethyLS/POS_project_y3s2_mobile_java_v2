package com.example.pos_project.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SaleItemRequest {
    @Expose
    @SerializedName("product_id")
    private int productId;
    
    @Expose
    @SerializedName("quantity")
    private int quantity;
    
    @Expose
    @SerializedName("unit_price")
    private double unitPrice;
    
    @Expose
    @SerializedName("tax_rate")
    private double taxRate;
    
    @Expose
    @SerializedName("discount_amount")
    private double discountAmount;

    public SaleItemRequest() {}

    public SaleItemRequest(int productId, int quantity, double unitPrice,
                          double taxRate, double discountAmount) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate;
        this.discountAmount = discountAmount;
    }

    // Getters and setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
}