package com.example.shoppimobile.repository;

import android.content.Context;

import com.example.shoppimobile.factory.APIClient;
import com.example.shoppimobile.model.Category;
import com.example.shoppimobile.model.Product;
import com.example.shoppimobile.service.CategoryService;
import com.example.shoppimobile.service.ProductService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class CategoryRepository {

    private CategoryService categoryService;

    public CategoryRepository(Context context) {
        Retrofit retrofit = APIClient.getClient(context);
        categoryService = retrofit.create(CategoryService.class);
    }

    public Call<List<Category>> getCategories() {
        return categoryService.getCategories();
    }
}
