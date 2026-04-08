package com.example.social_network.Model;

import java.io.Serializable;

public class CommentModel implements Serializable {
    private String username;
    private String commentText;
    private String timeAgo;
    private int likeCount;
    private String avatarUrl;

    public CommentModel(String username, String commentText, String timeAgo, int likeCount, String avatarUrl) {
        this.username = username;
        this.commentText = commentText;
        this.timeAgo = timeAgo;
        this.likeCount = likeCount;
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() { return username; }
    public String getCommentText() { return commentText; }
    public String getTimeAgo() { return timeAgo; }
    public int getLikeCount() { return likeCount; }
    public String getAvatarUrl() { return avatarUrl; }
}
