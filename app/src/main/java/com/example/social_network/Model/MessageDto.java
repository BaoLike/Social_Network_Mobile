package com.example.social_network.Model;

public class MessageDto {
    private final String id;
    private final String conversationId;
    private final String senderId;
    private final String content;
    private final String timestamp;
    /** From API {@code me}; if true, message is outgoing (right). */
    private final boolean fromMe;
    /** Avatar URL for incoming row; optional. */
    private final String incomingAvatarUrl;

    public MessageDto(String id, String conversationId, String senderId, String content, String timestamp,
                      boolean fromMe, String incomingAvatarUrl) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.fromMe = fromMe;
        this.incomingAvatarUrl = incomingAvatarUrl;
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

    public boolean isFromMe() {
        return fromMe;
    }

    public String getIncomingAvatarUrl() {
        return incomingAvatarUrl;
    }
}
