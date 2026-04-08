package com.example.social_network.Model;

public class User {
    private String id;
    private String username;
    private String location;
    private int avatarColor;
    private boolean isVerified;
    private String avatarUrl;

    public User(String id, String username, String location, int avatarColor, boolean isVerified) {
        this(id, username, location, avatarColor, isVerified, null);
    }

    public User(String id, String username, String location, int avatarColor, boolean isVerified, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.location = location;
        this.avatarColor = avatarColor;
        this.isVerified = isVerified;
        this.avatarUrl = avatarUrl;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getLocation() { return location; }
    public int getAvatarColor() { return avatarColor; }
    public boolean isVerified() { return isVerified; }
    public String getAvatarUrl() { return avatarUrl; }
}
