package com.example.shoppimobile.repository;

import android.content.Context;

import com.example.shoppimobile.factory.APIClient;
import com.example.shoppimobile.model.User;
import com.example.shoppimobile.service.UserService;

import retrofit2.Call;
import retrofit2.Retrofit;

public class UserRepository {
    private UserService userService;
    private Context context;

    public UserRepository(Context context) {
        this.context = context;
        Retrofit retrofit = APIClient.getClient(context);
        userService = retrofit.create(UserService.class);
    }

    public Call<User> createUser(User user) {
        return userService.createUser(user);
    }

    public Call<User> getUser(String token, String userId) {
        return userService.getUser("Bearer " + token, userId);
    }

    public Call<User> updateUser(String token, String userId, User user) {
        return userService.updateUser("Bearer " + token, userId, user);
    }
} 