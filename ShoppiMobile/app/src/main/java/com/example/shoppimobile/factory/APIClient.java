package com.example.shoppimobile.factory;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.example.shoppimobile.R;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        String baseURL = "http://"+context.getResources().getString(R.string.ip_address)+":3000/api/";

        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(90, TimeUnit.SECONDS) // Thay đổi thời gian chờ ở đây
                .readTimeout(90, TimeUnit.SECONDS) // Thời gian chờ đọc
                .writeTimeout(90, TimeUnit.SECONDS) // Thời gian chờ ghi
                .addInterceptor(new AuthInterceptor(context))
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
