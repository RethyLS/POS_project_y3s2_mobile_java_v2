package com.example.pos_project.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private int productId;
    private String productName;
    private String productImage;
    private double unitPrice;
    private int quantity;
    private double totalPrice;

    public CartItem(int productId, String productName, String productImage, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = unitPrice * quantity;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice;
        updateTotalPrice();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        updateTotalPrice();
    }

    public double getTotalPrice() { return totalPrice; }

    private void updateTotalPrice() {
        this.totalPrice = this.unitPrice * this.quantity;
    }
}