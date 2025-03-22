package com.example.shoppimobile.repository;

import android.content.Context;

import com.example.shoppimobile.factory.APIClient;
import com.example.shoppimobile.model.CartItem;
import com.example.shoppimobile.service.CartService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class CartRepository {
    private CartService cartService;

    public CartRepository(Context context) {
        Retrofit retrofit = APIClient.getClient(context);
        cartService = retrofit.create(CartService.class);
    }

    public Call<CartItem> addToCart(CartItem cartItem) {
        return cartService.addToCart(cartItem);
    }

    public Call<List<CartItem>> getCartItems() {
        return cartService.getCartItems();
    }

    public Call<CartItem> updateCartItem(String itemId, CartItem cartItem) {
        return cartService.updateCartItem(itemId, cartItem);
    }

    public Call<Void> deleteCartItem(String itemId) {
        return cartService.deleteCartItem(itemId);
    }
} 