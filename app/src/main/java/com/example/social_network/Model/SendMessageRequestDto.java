package com.example.social_network.Model;

public class SendMessageRequestDto {
    private final String conversationId;
    private final String message;

    public SendMessageRequestDto(String conversationId, String message) {
        this.conversationId = conversationId;
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getMessage() {
        return message;
    }
}
