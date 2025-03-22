package com.example.shoppimobile.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Message {
    @SerializedName("id")
    private String id;
    
    @SerializedName("userId")
    private String senderId;
    
    @SerializedName("receiverId")
    private String receiverId;
    
    @SerializedName("content")
    private String content;

    @SerializedName("createdAt")
    private Date timestamp;

//    @SerializedName("createdAt")
//    private long timestamp;
//
//    @SerializedName("updatedAt")
//    private long updatedAt;

    @SerializedName("isRead")
    private boolean isRead;
    
    // Used for UI display
    private boolean isSentByUser;

    public Message() {
    }

    public Message(String content) {
        this.content = content;
        this.timestamp = new Date();
    }

    public Message(String senderId, String receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = new Date();
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }

    public void setSentByUser(boolean sentByUser) {
        isSentByUser = sentByUser;
    }

    @NonNull
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + timestamp +
                ", isRead=" + isRead +
                ", isSentByUser=" + isSentByUser +
                '}';
    }
} 