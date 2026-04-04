package com.example.social_network.Model;

public class Story {
    private User user;
    private boolean isOwn;
    private boolean isLive;
    private boolean hasNewStory;

    public Story(User user, boolean isOwn, boolean isLive, boolean hasNewStory) {
        this.user = user;
        this.isOwn = isOwn;
        this.isLive = isLive;
        this.hasNewStory = hasNewStory;
    }

    public User getUser() { return user; }
    public boolean isOwn() { return isOwn; }
    public boolean isLive() { return isLive; }
    public boolean hasNewStory() { return hasNewStory; }
}
