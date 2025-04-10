package com.example.shoppimobile.service;

import com.example.shoppimobile.model.DashboardStats;
import com.example.shoppimobile.model.Order;
import com.example.shoppimobile.model.RevenueData;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OrderService {
    @POST("orders")
    Call<Order> createOrder(@Body Order order);

    @GET("orders")
    Call<List<Order>> getOrders();

    @GET("orders/{orderId}")
    Call<Order> getOrder(@Path("orderId") String orderId);

    @PUT("orders/{orderId}/status")
    Call<Order> updateOrder(@Path("orderId") String orderId, @Body Order order);
    
    @GET("orders/dashboard/stats")
    Call<DashboardStats> getDashboardStats();
    
    @GET("orders/dashboard/revenue")
    Call<List<RevenueData>> getRevenueData();
} 