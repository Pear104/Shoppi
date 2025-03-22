package com.example.shoppimobile.factory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Lấy token mới nhất
        String token = getToken();
//        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxYjI2MTViMS02M2M0LTQ1MjUtOTYxNC1kYmFlMWE4NmU3YzYiLCJlbWFpbCI6Im1pbmhxdWFuMjkxMDIwMDMyMDAzQGdtYWlsLmNvbSIsInJvbGUiOiJDdXN0b21lciIsImlhdCI6MTcyOTU4MTA5NSwiZXhwIjoxNzI5NTgxOTk1fQ.USLkAec6X2eVsCzciUI8lsF0zXvZskUWtSkUUVO7H5U";

        if (token == null) {
            Log.e("AuthInterceptor", "Token is null");
        }

        // Thêm token vào header
        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        // Ghi log phản hồi
        Response response = chain.proceed(newRequest);
        return response;
    }

    private String getToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);  // Trả về null nếu không có token
    }
}
