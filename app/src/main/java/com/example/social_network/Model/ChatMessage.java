package com.example.social_network.Model;

public class ChatMessage {
    private String text;
    private String imageUri; // if it's an image message
    private String timestamp;
    private boolean isOutgoing;
    /** For incoming server messages; null → placeholder in adapter */
    private String incomingAvatarUrl;

    public ChatMessage(String text, String imageUri, String timestamp, boolean isOutgoing) {
        this(text, imageUri, timestamp, isOutgoing, null);
    }

    public ChatMessage(String text, String imageUri, String timestamp, boolean isOutgoing,
                       String incomingAvatarUrl) {
        this.text = text;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
        this.isOutgoing = isOutgoing;
        this.incomingAvatarUrl = incomingAvatarUrl;
    }

    public String getText() { return text; }
    public String getImageUri() { return imageUri; }
    public String getTimestamp() { return timestamp; }
    public boolean isOutgoing() { return isOutgoing; }
    public String getIncomingAvatarUrl() { return incomingAvatarUrl; }
}
