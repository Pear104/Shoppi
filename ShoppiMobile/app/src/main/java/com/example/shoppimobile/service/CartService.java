package com.example.shoppimobile.service;

import com.example.shoppimobile.model.CartItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CartService {

    @GET("cart")
    Call<List<CartItem>> getCartItems();

    @DELETE("cart")
    Call<List<Void>> deleteCartItem();

    @POST("cart/items")
    Call<CartItem> addToCart(@Body CartItem cartItem);

    @PUT("cart/items/{itemId}")
    Call<CartItem> updateCartItem(
            @Path("itemId") String itemId,
            @Body CartItem cartItem
    );

    @DELETE("cart/items/{itemId}")
    Call<Void> deleteCartItem(@Path("itemId") String itemId);
} 