package com.example.social_network.Model;

public class MessageDto {
    private final String id;
    private final String conversationId;
    private final String senderId;
    private final String content;
    private final String timestamp;

    public MessageDto(String id, String conversationId, String senderId, String content, String timestamp) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
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

    public String getTimestamp() {
        return timestamp;
    }
}
