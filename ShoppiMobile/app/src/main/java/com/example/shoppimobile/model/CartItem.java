package com.example.shoppimobile.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CartItem {

    @SerializedName("id")
    private String id;

    @SerializedName("product")
    private Product product;

    @SerializedName("productId")
    private String productId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Constructors
    public CartItem() {}

    public CartItem(int quantity) {
        this.quantity = quantity;
    }

    public CartItem(int quantity, String productId) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public CartItem(Date createdAt, String id, Product product, int quantity, Date updatedAt) {
        this.createdAt = createdAt;
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}