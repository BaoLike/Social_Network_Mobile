package com.example.social_network.Model;

public class ChatMessage {
    private String text;
    private String imageUri; // if it's an image message
    private String timestamp;
    private boolean isOutgoing;

    public ChatMessage(String text, String imageUri, String timestamp, boolean isOutgoing) {
        this.text = text;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
        this.isOutgoing = isOutgoing;
    }

    public String getText() { return text; }
    public String getImageUri() { return imageUri; }
    public String getTimestamp() { return timestamp; }
    public boolean isOutgoing() { return isOutgoing; }
}
