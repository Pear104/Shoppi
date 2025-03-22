package com.example.shoppimobile.repository;

import android.content.Context;

import com.example.shoppimobile.factory.APIClient;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.service.ProductService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class ProductRepository {

    private ProductService productService;

    public ProductRepository(Context context) {
        Retrofit retrofit = APIClient.getClient(context);
        productService = retrofit.create(ProductService.class);
    }

    public Call<List<Product>> getProducts(String category, String search) {
        return productService.getProducts(category, search);
    }

    public Call<Product> getProductById(String productId) {
        return productService.getProductById(productId);
    }
}
