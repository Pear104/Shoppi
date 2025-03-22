package com.example.shoppimobile.service;

import com.example.shoppimobile.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {
    @GET("products")
    Call<List<Product>> getProducts(@Query("category") String category, @Query("search") String search);

    @GET("products/{productId}")
    Call<Product> getProductById(@Path("productId") String productId);
}
