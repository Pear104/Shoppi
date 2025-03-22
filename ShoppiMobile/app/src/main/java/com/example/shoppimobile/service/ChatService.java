package com.example.shoppimobile.service;

import com.example.shoppimobile.model.Message;
import com.example.shoppimobile.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatService {
    @GET("chat/contacts")
    Call<List<User>> getContacts();

    @GET("chat/user/{userId}")
    Call<List<Message>> getChatMessages(@Path("userId") String roomId);

    @POST("chat/user/{userId}")
    Call<Message> sendMessage(@Path("userId") String roomId, @Body Message message);
}
