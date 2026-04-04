package com.example.social_network.Model;

public class ConversationRequestDto {
    private final String senderId;
    private final String receiverId;

    public ConversationRequestDto(String senderId, String receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }
}
