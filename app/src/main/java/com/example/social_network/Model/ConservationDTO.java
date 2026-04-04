package com.example.social_network.Model;

public class ConservationDTO {
    /** Partner user id (for chat); may be null for legacy payloads */
    private String userId;
    /** Mongo / server conversation id for GET messages; optional */
    private String conversationId;
    private String conversationAvatar;
    private String userName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getConversationAvatar() {
        return conversationAvatar;
    }

    public ConservationDTO(String conversationAvatar, String userName) {
        this(null, conversationAvatar, userName, null);
    }

    public ConservationDTO(String userId, String conversationAvatar, String userName) {
        this(userId, conversationAvatar, userName, null);
    }

    public ConservationDTO(String userId, String conversationAvatar, String userName, String conversationId) {
        this.userId = userId;
        this.conversationAvatar = conversationAvatar;
        this.userName = userName;
        this.conversationId = conversationId;
    }

    public void setConversationAvatar(String conversationAvatar) {
        this.conversationAvatar = conversationAvatar;
    }
}
