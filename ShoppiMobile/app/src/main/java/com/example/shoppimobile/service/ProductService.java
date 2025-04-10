package com.example.shoppimobile.service;

import com.example.shoppimobile.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {
    @GET("products")
    Call<List<Product>> getProducts(@Query("category") String category, @Query("search") String search);

    @GET("products/{productId}")
    Call<Product> getProductById(@Path("productId") String productId);

    @POST("products")
    Call<Product> createProduct(@Body Product product);

    @PUT("products/{productId}")
    Call<Product> updateProduct(@Path("productId") String productId, @Body Product product);
}
