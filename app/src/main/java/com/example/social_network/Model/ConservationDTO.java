package com.example.social_network.Model;

public class ConservationDTO {
    /** Partner user id (for chat); may be null for legacy payloads */
    private String userId;
    private String conversationAvatar;
    private String userName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        this(null, conversationAvatar, userName);
    }

    public ConservationDTO(String userId, String conversationAvatar, String userName) {
        this.userId = userId;
        this.conversationAvatar = conversationAvatar;
        this.userName = userName;
    }

    public void setConversationAvatar(String conversationAvatar) {
        this.conversationAvatar = conversationAvatar;
    }
}
