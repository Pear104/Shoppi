package com.example.shoppimobile.model;

import android.content.Context;
import android.util.Log;

import com.example.shoppimobile.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class User {
    private String id;
    private String email;
    private String password;
    private String name;
    private String photoUrl;
    private String role;
    private Date createdAt;
    private Date updatedAt;
    private Date lastLoginAt;
    private List<Order> orders;
    private List<Review> reviews;
    private Cart cart;
    private List<Message> sentMessages;
    private List<Message> inboxMessages;

    @SerializedName("lastMessage")
    private Message lastMessage;
    private transient UserRepository userRepository;

    // Enum for User Role
    public enum Role {
        ADMIN, CUSTOMER
    }

    // Constructors
    public User() {
    }

    public User(String id, String email, String name, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role.name();
    }

    public void initRepository(Context context) {
        userRepository = new UserRepository(context);
    }

    public void createUserInDatabase(Context context, FirebaseUser firebaseUser) {
        initRepository(context);

        setId(firebaseUser.getUid());
        setEmail(firebaseUser.getEmail());
        setName(firebaseUser.getDisplayName());
        setPhotoUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
        setRole("user");  // Default role
        setCreatedAt(new Date());
        setLastLoginAt(new Date());

        // Make API call to your backend
        Call<User> call = userRepository.createUser(this);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // User created in database successfully
                    Log.d("Auth", "User created in database");
                } else {
                    // Handle error
                    Log.e("Auth", "Failed to create user in database");
                    // You might want to delete the Firebase user if database creation fails
                    firebaseUser.delete();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Handle network error
                Log.e("Auth", "Network error creating user in database");
                firebaseUser.delete();
            }
        });
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<Message> getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(List<Message> sentMessages) {
        this.sentMessages = sentMessages;
    }

    public List<Message> getInboxMessages() {
        return inboxMessages;
    }

    public void setInboxMessages(List<Message> inboxMessages) {
        this.inboxMessages = inboxMessages;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + lastMessage.getContent() +
                '}';
    }
} 