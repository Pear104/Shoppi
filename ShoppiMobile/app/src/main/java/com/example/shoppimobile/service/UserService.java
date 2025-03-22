package com.example.shoppimobile.service;

import com.example.shoppimobile.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {
    @POST("api/users")
    Call<User> createUser(@Body User user);

    @GET("api/users/{userId}")
    Call<User> getUser(
        @Header("Authorization") String token,
        @Path("userId") String userId
    );

    @PUT("api/users/{userId}")
    Call<User> updateUser(
        @Header("Authorization") String token,
        @Path("userId") String userId,
        @Body User user
    );
} 