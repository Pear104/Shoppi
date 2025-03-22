package com.example.shoppimobile.repository;

import android.content.Context;

import com.example.shoppimobile.factory.APIClient;
import com.example.shoppimobile.model.Message;
import com.example.shoppimobile.model.User;
import com.example.shoppimobile.service.ChatService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class ChatRepository {

    private ChatService chatService;

    public ChatRepository(Context context) {
        Retrofit retrofit = APIClient.getClient(context);
        chatService = retrofit.create(ChatService.class);
    }

    public Call<List<User>> getContacts() {
        return chatService.getContacts();
    }

    public Call<List<Message>> getChatMessages(String userId) {
        return chatService.getChatMessages(userId);
    }

    public Call<Message> sendMessage(String userId, Message message) {
        return chatService.sendMessage(userId, message);
    }
}
