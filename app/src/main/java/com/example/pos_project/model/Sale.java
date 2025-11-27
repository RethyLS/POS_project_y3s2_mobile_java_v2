package com.example.pos_project.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

@Entity(tableName = "sales")
public class Sale {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @SerializedName("transaction_id")
    @Expose
    private String transactionId;

    @SerializedName("sale_date")
    @Expose
    private String saleDate;

    @SerializedName("total_amount")
    @Expose
    private double totalAmount;

    @SerializedName("payment_method")
    @Expose
    private String paymentMethod; // "cash", "card", "digital"

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    @SerializedName("store_id")
    @Expose
    private int storeId;

    @SerializedName("customer_id")
    @Expose
    private Integer customerId;

    @SerializedName("cashier_id")
    @Expose
    private int cashierId;

    @SerializedName("subtotal_amount")
    @Expose
    private String subtotalAmount;

    @SerializedName("tax_amount")
    @Expose
    private String taxAmount;

    @SerializedName("discount_amount")
    @Expose
    private String discountAmount;

    // For local database compatibility
    private double paidAmount;
    private double changeAmount;
    private int userId; // ID of the user who made the sale
    private String customerName;

    // Constructors
    public Sale() {}

    @Ignore
    public Sale(String transactionId, String saleDate, double totalAmount,
                String paymentMethod, String status, String createdAt) {
        this.transactionId = transactionId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
    }

    @Ignore
    public Sale(String saleDate, double totalAmount, double paidAmount,
                String paymentMethod, int userId, String customerName) {
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.changeAmount = paidAmount - totalAmount;
        this.paymentMethod = paymentMethod;
        this.userId = userId;
        this.customerName = customerName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSaleDate() { return saleDate; }
    public void setSaleDate(String saleDate) { this.saleDate = saleDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getChangeAmount() { return changeAmount; }
    public void setChangeAmount(double changeAmount) { this.changeAmount = changeAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    // New API fields
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Additional API fields
    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public int getCashierId() { return cashierId; }
    public void setCashierId(int cashierId) { this.cashierId = cashierId; }

    public String getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(String subtotalAmount) { this.subtotalAmount = subtotalAmount; }

    public String getTaxAmount() { return taxAmount; }
    public void setTaxAmount(String taxAmount) { this.taxAmount = taxAmount; }

    public String getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(String discountAmount) { this.discountAmount = discountAmount; }
}