package com.example.social_network.Model;

public class SendMessageRequestDto {
    private final String conversationId;
    private final String senderId;
    private final String content;

    public SendMessageRequestDto(String conversationId, String senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }
}
