package com.example.shoppimobile.Helper;


import android.util.Log;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {
    private static SocketManager instance;
    private Socket socket;
    private String token;

    private static final String SERVER_URL = "http://your-server-url:3000";

    private SocketManager() {
        // Private constructor for singleton
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void connect() {
        if (socket == null || !socket.connected()) {
            try {
                IO.Options options = new IO.Options();
                options.forceNew = true;
                options.reconnection = true;

                // Set authentication token
                JSONObject auth = new JSONObject();
                auth.put("token", token);
                options.auth = (java.util.Map<String, String>) auth;

                socket = IO.socket(SERVER_URL, options);
                setupSocketEvents();
                socket.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    private void setupSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d("SocketIO", "Connected to server");
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Log.d("SocketIO", "Disconnected from server");
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Log.e("SocketIO", "Connection error: " + args[0]);
        });

        // Set up custom event listeners
        socket.on("new_message", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                // Parse message data
                // Notify UI about new message
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        socket.on("user_typing", args -> {
            // Handle typing indicator
        });

        socket.on("error", args -> {
            Log.e("SocketIO", "Server error: " + args[0]);
        });
    }

    // Join a chat with another user
    public void startChat(String receiverId) {
        if (socket != null && socket.connected()) {
            socket.emit("start_chat", receiverId);
        }
    }

    // Send typing indicator
    public void sendTyping(String receiverId) {
        if (socket != null && socket.connected()) {
            socket.emit("typing", receiverId);
        }
    }

    // Get the socket instance to emit custom events
    public Socket getSocket() {
        return socket;
    }
}
