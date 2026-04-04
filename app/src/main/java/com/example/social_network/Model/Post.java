package com.example.social_network.Model;

import java.util.List;

public class Post {
    private User user;
    private List<Integer> imageColors;
    private String likedByUsername;
    private int likeCount;
    private String caption;
    private String timeAgo;
    private boolean isLiked;
    private boolean isBookmarked;

    public Post(User user, List<Integer> imageColors, String likedByUsername,
                int likeCount, String caption, String timeAgo,
                boolean isLiked, boolean isBookmarked) {
        this.user = user;
        this.imageColors = imageColors;
        this.likedByUsername = likedByUsername;
        this.likeCount = likeCount;
        this.caption = caption;
        this.timeAgo = timeAgo;
        this.isLiked = isLiked;
        this.isBookmarked = isBookmarked;
    }

    public User getUser() { return user; }
    public List<Integer> getImageColors() { return imageColors; }
    public String getLikedByUsername() { return likedByUsername; }
    public int getLikeCount() { return likeCount; }
    public String getCaption() { return caption; }
    public String getTimeAgo() { return timeAgo; }
    public boolean isLiked() { return isLiked; }
    public boolean isBookmarked() { return isBookmarked; }
    public int getImageCount() { return imageColors.size(); }

    public void setLiked(boolean liked) { this.isLiked = liked; }
    public void setBookmarked(boolean bookmarked) { this.isBookmarked = bookmarked; }
    public void setLikeCount(int count) { this.likeCount = count; }
}
