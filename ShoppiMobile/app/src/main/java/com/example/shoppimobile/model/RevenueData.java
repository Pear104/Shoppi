package com.example.shoppimobile.model;

public class RevenueData {
    private String date;
    private float amount;

    public RevenueData() {
    }

    public RevenueData(String date, float amount) {
        this.date = date;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
} 