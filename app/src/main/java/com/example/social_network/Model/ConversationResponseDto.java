package com.example.social_network.Model;

public class ConversationResponseDto {
    private final String conversationId;

    public ConversationResponseDto(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }
}
