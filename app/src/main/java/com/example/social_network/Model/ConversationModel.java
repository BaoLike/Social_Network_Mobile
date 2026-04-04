package com.example.social_network.Model;

/**
 * Represents a single Direct Message conversation entry.
 */
public class ConversationModel {

    private final String receiverId;
    private final String username;
    private final String lastMessage;
    private final String timeAgo;
    /** ARGB color used for the avatar circle background */
    private final int avatarColor;
    /** Server conversation id when known (skip create + correct GET messages). */
    private final String conversationId;

    public ConversationModel(String receiverId, String username, String lastMessage,
                             String timeAgo, int avatarColor) {
        this(receiverId, username, lastMessage, timeAgo, avatarColor, null);
    }

    public ConversationModel(String receiverId, String username, String lastMessage,
                             String timeAgo, int avatarColor, String conversationId) {
        this.receiverId = receiverId;
        this.username = username;
        this.lastMessage = lastMessage;
        this.timeAgo = timeAgo;
        this.avatarColor = avatarColor;
        this.conversationId = conversationId;
    }

    public String getReceiverId()  { return receiverId; }
    public String getUsername()    { return username; }
    public String getLastMessage() { return lastMessage; }
    public String getTimeAgo()     { return timeAgo; }
    public int    getAvatarColor() { return avatarColor; }
    public String getConversationId() { return conversationId; }
}
