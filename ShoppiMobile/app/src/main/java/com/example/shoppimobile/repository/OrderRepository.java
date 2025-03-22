package com.example.shoppimobile.repository;

import android.content.Context;

import com.example.shoppimobile.factory.APIClient;
import com.example.shoppimobile.model.Order;
import com.example.shoppimobile.service.OrderService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class OrderRepository {
    private final OrderService orderService;

    public OrderRepository(Context context) {
        Retrofit retrofit = APIClient.getClient(context);
        orderService = retrofit.create(OrderService.class);
    }

    public Call<Order> createOrder(Order order) {
        return orderService.createOrder( order);
    }

    public Call<List<Order>> getOrders() {
        return orderService.getOrders();
    }

    public Call<Order> getOrder(String orderId) {
        return orderService.getOrder(orderId);
    }

    public Call<Order> updateOrder(String orderId, Order order) {
        return orderService.updateOrder(orderId, order);
    }
} 