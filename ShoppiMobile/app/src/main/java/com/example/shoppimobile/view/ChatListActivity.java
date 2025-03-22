package com.example.shoppimobile.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppimobile.R;
import com.example.shoppimobile.adapter.UserAdapter;
import com.example.shoppimobile.model.User;
import com.example.shoppimobile.repository.ChatRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListActivity extends AppCompatActivity {

    private String TAG = "ChatListActivity";
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private ChatRepository chatRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Customer Support");
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatRepository = new ChatRepository(this);
        userAdapter = new UserAdapter(userList);
        recyclerView.setAdapter(userAdapter);

        loadContacts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void loadContacts() {
        Call<List<User>> call = chatRepository.getContacts();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> contacts = response.body();
//                    for (User contact : contacts) {
//                        Log.d(TAG, contact.toString());
//                    }
                    userList.clear();
                    userList.addAll(contacts);
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ChatListActivity.this,"Fetch error",Toast.LENGTH_SHORT).show();
                // Handle error
            }
        });
    }
}