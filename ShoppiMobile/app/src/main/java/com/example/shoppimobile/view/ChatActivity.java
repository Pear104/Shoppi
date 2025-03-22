package com.example.shoppimobile.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppimobile.R;
import com.example.shoppimobile.adapter.MessageAdapter;
import com.example.shoppimobile.model.Message;
import com.example.shoppimobile.repository.ChatRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static String SOCKET_URL;
    private String adminId;
    private String senderId;
    private String receiverId;
    private String accessToken;

    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private EditText messageInput;
    private FloatingActionButton sendButton;
    private ChatRepository chatRepository;
    private Socket socket;
    private String chatRoomId = "support"; // Default chat room ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Setup
        adminId = getResources().getString(R.string.admin_id);
        SOCKET_URL = "http://"+getResources().getString(R.string.ip_address)+":3000";
        chatRepository = new ChatRepository(this);
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        senderId = sharedPreferences.getString("id", "");
        String userRole = sharedPreferences.getString("role", "");
        accessToken = sharedPreferences.getString("access_token", "");

        // Set up receiverId
        if (userRole.toLowerCase().equals("admin")) {
            senderId = adminId;
            receiverId = getIntent().getStringExtra("RECEIVER_ID");
            chatRoomId = receiverId+"-"+senderId;
        } else {
            receiverId = adminId;
            chatRoomId = senderId+"-"+receiverId;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Customer Support");
        }

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        // Set up RecyclerView
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Initialize Socket connection
        initializeSocket();

        // Load previous messages
        loadPreviousMessages();

        // Set up send button
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void initializeSocket() {
        try {
            // Create socket connection options
            IO.Options options = new IO.Options();
            Map<String, String> authMap = new HashMap<>();
            authMap.put("token", accessToken);
            options.reconnection = true;
            options.forceNew = true;
            options.auth = authMap;

            // Connect to the socket server
            socket = IO.socket(SOCKET_URL, options);

            // Set up socket event listeners
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on("message", onNewMessage);

            // Connect to the socket server
            socket.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Error initializing socket: " + e.getMessage());
            Toast.makeText(this, "Failed to connect to chat server", Toast.LENGTH_SHORT).show();
        }
    }

    private Emitter.Listener onConnect = args -> runOnUiThread(() -> {
        Log.d(TAG, "Socket connected");
        // Join the support chat room
        JSONObject data = new JSONObject();
        try {
            data.put("userId", senderId);
            data.put("roomId", chatRoomId);
            socket.emit("join", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error joining room: " + e.getMessage());
        }
    });

    private Emitter.Listener onDisconnect = args -> runOnUiThread(() -> {
        Log.d(TAG, "Socket disconnected");
    });

    private Emitter.Listener onConnectError = args -> runOnUiThread(() -> {
        Log.e(TAG, "Socket connection error");
        Toast.makeText(ChatActivity.this, "Connection error. Check your network", Toast.LENGTH_SHORT).show();
    });

    private Emitter.Listener onNewMessage = args -> runOnUiThread(() -> {
        try {
            // Parse the received JSON message
            JSONObject data = (JSONObject) args[0];
            String senderId1 = data.getString("senderId");
            String content = data.getString("content");
//            Date timestamp = new Date(data.getString("timestamp"));
            long timestamp = data.getLong("timestamp");
            Log.d("Sender Id: ",senderId1);

            // Create a message object
            Message message = new Message(content);
            message.setSenderId(senderId1);
            Date timestampDate = new Date(timestamp);
            message.setTimestamp(timestampDate);
            // Mark if this message is from the current user
            message.setSentByUser(senderId1 != null && senderId1.equals(senderId));

            // Add message to the list
            messageAdapter.addMessage(message);
            scrollToBottom();

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing message: " + e.getMessage());
        }
    });

    private void loadPreviousMessages() {
        Call<List<Message>> call = chatRepository.getChatMessages(receiverId);
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> messages = response.body();
                    Log.d(TAG, "Current user "+ senderId);
                    for (Message message : messages) {
                        message.setSentByUser(senderId != null && message.getSenderId().equals(senderId));
                        Log.d(TAG, "Message: " + message.toString());
                    }
                    // Update adapter
                    messageAdapter.setMessages(messages);
                    scrollToBottom();

                } else {
                    // Load initial welcome message instead
                    loadInitialMessages();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Log.e(TAG, "Error loading messages: " + t.getMessage());
                // Load initial welcome message instead
                loadInitialMessages();
            }
        });

        loadInitialMessages();
    }

    private void loadInitialMessages() {
        // Create welcome message for new users
        List<Message> messages = new ArrayList<>();

        Message welcomeMessage = new Message("Welcome to Shoppi Customer Support! How can we help you today?");
        welcomeMessage.setSenderId("system");
        welcomeMessage.setSentByUser(false);
        welcomeMessage.setTimestamp(new Date()); // 1 minute ago
//        welcomeMessage.setTimestamp(System.currentTimeMillis() - 60000); // 1 minute ago
        messages.add(welcomeMessage);

        messageAdapter.setMessages(messages);
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Clear input field
        messageInput.setText("");

        // Create message object
        Message message = new Message(messageText);
        message.setSentByUser(true);
        message.setSenderId(senderId);
        message.setTimestamp(new Date());
//        message.setTimestamp(System.currentTimeMillis());

        // Send message to server via API
        Call<Message> call = chatRepository.sendMessage(receiverId, message);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Error sending message via API");
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Log.e(TAG, "Failed to send message: " + t.getMessage());
                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });

        // Send message via Socket
        try {
            JSONObject data = new JSONObject();
            data.put("roomId", chatRoomId);
            data.put("senderId", senderId);
            data.put("content", messageText);
            data.put("timestamp", System.currentTimeMillis());

            socket.emit("message", data);
        } catch (JSONException e) {
            Log.e(TAG, "Error sending message via socket: " + e.getMessage());
        }
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect from the socket server when activity is destroyed
        if (socket != null) {
            socket.disconnect();
            socket.off(Socket.EVENT_CONNECT, onConnect);
            socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.off("message", onNewMessage);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}