package com.example.shoppimobile.model;

public class DashboardStats {
    private double totalRevenue;
    private int totalOrders;
    private double todayRevenue;

    public DashboardStats() {
    }

    public DashboardStats(double totalRevenue, int totalOrders, double todayRevenue) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.todayRevenue = todayRevenue;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }
} 