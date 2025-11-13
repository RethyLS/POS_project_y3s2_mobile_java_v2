package com.example.pos_project.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SaleRequest {
    @Expose
    @SerializedName("customer_id")
    private Integer customerId;
    
    @Expose
    @SerializedName("cashier_id")
    private int cashierId;
    
    @Expose
    @SerializedName("payment_method")
    private String paymentMethod;
    
    @Expose
    @SerializedName("items")
    private List<SaleItemRequest> items;
    
    @Expose
    @SerializedName("subtotal_amount")
    private double subtotalAmount;
    
    @Expose
    @SerializedName("tax_amount")
    private double taxAmount;
    
    @Expose
    @SerializedName("discount_amount")
    private double discountAmount;
    
    @Expose
    @SerializedName("total_amount")
    private double totalAmount;
    
    @Expose
    @SerializedName("notes")
    private String notes;

    public SaleRequest() {}

    public SaleRequest(Integer customerId, int cashierId, String paymentMethod,
                      List<SaleItemRequest> items, double subtotalAmount,
                      double taxAmount, double discountAmount, double totalAmount, String notes) {
        this.customerId = customerId;
        this.cashierId = cashierId;
        this.paymentMethod = paymentMethod;
        this.items = items;
        this.subtotalAmount = subtotalAmount;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.notes = notes;
    }

    // Getters and setters
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public int getCashierId() { return cashierId; }
    public void setCashierId(int cashierId) { this.cashierId = cashierId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }

    public double getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(double subtotalAmount) { this.subtotalAmount = subtotalAmount; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}