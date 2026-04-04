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

    public ConversationModel(String receiverId, String username, String lastMessage,
                             String timeAgo, int avatarColor) {
        this.receiverId = receiverId;
        this.username    = username;
        this.lastMessage = lastMessage;
        this.timeAgo     = timeAgo;
        this.avatarColor = avatarColor;
    }

    public String getReceiverId()  { return receiverId; }
    public String getUsername()    { return username; }
    public String getLastMessage() { return lastMessage; }
    public String getTimeAgo()     { return timeAgo; }
    public int    getAvatarColor() { return avatarColor; }
}
