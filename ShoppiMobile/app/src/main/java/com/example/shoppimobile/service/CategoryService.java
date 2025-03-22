package com.example.shoppimobile.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.shoppimobile.model.Category;
import com.example.shoppimobile.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryService {

    @GET("categories")
    Call<List<Category>> getCategories();
}