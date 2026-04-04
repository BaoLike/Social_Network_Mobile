package com.example.social_network.Model;

/**
 * Represents a single post – used both in the 3-column grid and the detail view.
 */
public class PostGridModel {

    public enum Type { IMAGE, VIDEO, MULTI }

    private final String imageUrl;
    private final Type   type;
    private       int    likeCount;
    private       boolean isLiked;
    private       boolean isBookmarked;
    private final String username;
    private final String caption;
    private final String timeAgo;

    // ── Constructor for mock / restored posts ─────────────────────────────
    public PostGridModel(String imageUrl, Type type, int likeCount,
                         String username, String caption, String timeAgo) {
        this.imageUrl    = imageUrl;
        this.type        = type;
        this.likeCount   = likeCount;
        this.isLiked     = false;
        this.isBookmarked = false;
        this.username    = username;
        this.caption     = caption;
        this.timeAgo     = timeAgo;
    }

    // ── Legacy constructor (backward-compatible) ───────────────────────────
    public PostGridModel(String imageUrl, Type type, int likeCount) {
        this(imageUrl, type, likeCount, "jacob_w", "", "just now");
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String  getImageUrl()   { return imageUrl; }
    public Type    getType()       { return type; }
    public int     getLikeCount()  { return likeCount; }
    public boolean isLiked()       { return isLiked; }
    public boolean isBookmarked()  { return isBookmarked; }
    public String  getUsername()   { return username; }
    public String  getCaption()    { return caption; }
    public String  getTimeAgo()    { return timeAgo; }

    // ── Setters ───────────────────────────────────────────────────────────
    public void setLiked(boolean liked)          { this.isLiked = liked; }
    public void setBookmarked(boolean b)         { this.isBookmarked = b; }
    public void setLikeCount(int count)          { this.likeCount = count; }
}
