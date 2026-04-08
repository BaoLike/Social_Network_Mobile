package com.example.social_network.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Activity.StoryViewerActivity;
import com.example.social_network.Model.Post;
import com.example.social_network.Model.Story;
import com.example.social_network.R;
import com.example.social_network.Utils.FetchApi;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String STORY_FALLBACK_MEDIA =
            "https://images.unsplash.com/photo-1497366216548-37526070297c?w=1200";


    private static final int VIEW_TYPE_STORIES = 0;
    private static final int VIEW_TYPE_POST    = 1;

    private final List<Story> stories;
    private final List<Post>  posts;

    public FeedAdapter(List<Story> stories, List<Post> posts) {
        this.stories = stories;
        this.posts   = posts;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_STORIES : VIEW_TYPE_POST;
    }

    @Override
    public int getItemCount() {
        return posts.size() + 1; // +1 for stories header
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_STORIES) {
            View view = inflater.inflate(R.layout.item_stories_header, parent, false);
            return new StoriesViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof StoriesViewHolder) {
            ((StoriesViewHolder) holder).bind(stories);
        } else if (holder instanceof PostViewHolder) {
            ((PostViewHolder) holder).bind(posts.get(position - 1));
        }
    }

    // ----------------------------- Stories ViewHolder -----------------------------

    static class StoriesViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView rvStories;

        StoriesViewHolder(@NonNull View itemView) {
            super(itemView);
            rvStories = itemView.findViewById(R.id.rvStories);
        }

        void bind(List<Story> stories) {
            StoryAdapter adapter = new StoryAdapter(stories);
            rvStories.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvStories.setAdapter(adapter);
            adapter.setOnStoryClickListener((story, position) -> {
                Intent intent = new Intent(itemView.getContext(), StoryViewerActivity.class);
                intent.putExtra(StoryViewerActivity.EXTRA_MEDIA_URL, STORY_FALLBACK_MEDIA);
                intent.putExtra(StoryViewerActivity.EXTRA_AVATAR_URL, story.getUser().getAvatarUrl());
                intent.putExtra(StoryViewerActivity.EXTRA_USERNAME, story.getUser().getUsername());
                intent.putExtra(StoryViewerActivity.EXTRA_TIME, "4h");
                itemView.getContext().startActivity(intent);
            });
        }
    }

    // ----------------------------- Post ViewHolder -----------------------------

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "FeedAdapter";
        private final ImageView   ivPostAvatar;
        private final TextView    tvPostUsername;
        private final ImageView   ivVerified;
        private final TextView    tvPostLocation;
        private final ImageButton btnMore;
        private final ImageView   ivPostImage;
        private final TextView    tvImageCounter;
        private final LinearLayout llDotIndicators;
        private final ImageButton btnLike;
        private final ImageButton btnBookmark;
        private final TextView    tvLikes;
        private final TextView    tvCaption;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostAvatar    = itemView.findViewById(R.id.ivPostAvatar);
            tvPostUsername  = itemView.findViewById(R.id.tvPostUsername);
            ivVerified      = itemView.findViewById(R.id.ivVerified);
            tvPostLocation  = itemView.findViewById(R.id.tvPostLocation);
            btnMore         = itemView.findViewById(R.id.btnMore);
            ivPostImage     = itemView.findViewById(R.id.ivPostImage);
            tvImageCounter  = itemView.findViewById(R.id.tvImageCounter);
            llDotIndicators = itemView.findViewById(R.id.llDotIndicators);
            btnLike         = itemView.findViewById(R.id.btnLike);
            btnBookmark     = itemView.findViewById(R.id.btnBookmark);
            tvLikes         = itemView.findViewById(R.id.tvLikes);
            tvCaption       = itemView.findViewById(R.id.tvCaption);
        }

        void bind(Post post) {
            // Avatar
            String avatarUrl = post.getUser().getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                Picasso.get().load(avatarUrl).centerCrop().fit().into(ivPostAvatar);
            } else {
                GradientDrawable avatarBg = new GradientDrawable();
                avatarBg.setShape(GradientDrawable.OVAL);
                avatarBg.setColor(post.getUser().getAvatarColor());
                ivPostAvatar.setBackground(avatarBg);
                ivPostAvatar.setImageDrawable(null);
            }

            // Username
            tvPostUsername.setText(post.getUser().getUsername());

            // Verified badge
            ivVerified.setVisibility(post.getUser().isVerified() ? View.VISIBLE : View.GONE);

            // Location
            String location = post.getUser().getLocation();
            if (location != null && !location.isEmpty()) {
                tvPostLocation.setText(location);
                tvPostLocation.setVisibility(View.VISIBLE);
            } else {
                tvPostLocation.setVisibility(View.GONE);
            }

            // Post media (from API) with fallback placeholder
            if (post.getImageUrl() != null && !post.getImageUrl().trim().isEmpty()) {
                ivPostImage.setBackgroundColor(Color.TRANSPARENT);
                Picasso.get().load(post.getImageUrl()).centerCrop().fit().into(ivPostImage);
            } else {
                ivPostImage.setBackgroundColor(post.getImageColors().get(0));
                ivPostImage.setImageDrawable(null);
            }

            // Multi-image counter
            int imageCount = post.getImageCount();
            if (imageCount > 1) {
                tvImageCounter.setText(String.format(Locale.getDefault(), "1/%d", imageCount));
                tvImageCounter.setVisibility(View.VISIBLE);
                buildDotIndicators(imageCount);
                llDotIndicators.setVisibility(View.VISIBLE);
            } else {
                tvImageCounter.setVisibility(View.GONE);
                llDotIndicators.setVisibility(View.GONE);
            }

            // Like button
            updateLikeButton(post);
            btnLike.setOnClickListener(v -> {
                String postId = post.getPostId();
                if (postId == null || postId.trim().isEmpty()) {
                    Toast.makeText(itemView.getContext(), "Thiếu postId để like", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean willLike = !post.isLiked();
                // Optimistic update: update UI immediately, request continues in background.
                post.setLiked(willLike);
                int current = post.getLikeCount();
                int next = willLike ? (current + 1) : Math.max(0, current - 1);
                post.setLikeCount(next);
                updateLikeButton(post);
                updateLikesText(post);

                FetchApi api = new FetchApi();
                FetchApi.ApiCallback cb = new FetchApi.ApiCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(String message) {
                        // Keep UI responsive; just inform user if server call fails.
                        Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                };

                if (willLike) {
                    api.putLikePost(itemView.getContext(), postId, TAG, cb);
                } else {
                    api.putUnlikePost(itemView.getContext(), postId, TAG, cb);
                }
            });

            // Bookmark button
            updateBookmarkButton(post);
            btnBookmark.setOnClickListener(v -> {
                post.setBookmarked(!post.isBookmarked());
                updateBookmarkButton(post);
            });

            // Comment button
            ImageButton btnComment = itemView.findViewById(R.id.btnComment);
            if (btnComment != null) {
                btnComment.setOnClickListener(v -> {
                    android.content.Context ctx = itemView.getContext();
                    if (ctx instanceof androidx.fragment.app.FragmentActivity) {
                        com.example.social_network.Fragment.CommentsBottomSheet sheet =
                                com.example.social_network.Fragment.CommentsBottomSheet
                                        .newInstance(post.getPostId(), post.getComments());
                        sheet.show(((androidx.fragment.app.FragmentActivity) ctx).getSupportFragmentManager(), "CommentsBottomSheet");
                    }
                });
            }

            // More button
            btnMore.setOnClickListener(v ->
                    Toast.makeText(itemView.getContext(), "More options", Toast.LENGTH_SHORT).show());

            // Likes text
            updateLikesText(post);

            // Caption with bold username
            buildCaption(post);
        }

        private void updateLikeButton(Post post) {
            btnLike.setImageResource(post.isLiked()
                    ? R.drawable.ic_heart_filled
                    : R.drawable.ic_heart_outline);
        }

        private void updateBookmarkButton(Post post) {
            btnBookmark.setImageResource(post.isBookmarked()
                    ? R.drawable.ic_bookmark_filled
                    : R.drawable.ic_bookmark_outline);
        }

        private void updateLikesText(Post post) {
            int count = post.getLikeCount();
            String liked = post.getLikedByUsername();
            String formatted = formatCount(count);
            String full = "Liked by " + liked + " and " + formatted + " others";

            SpannableString spannable = new SpannableString(full);
            // Bold "liked by" username
            int start = full.indexOf(liked);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    start, start + liked.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Bold count
            int countStart = full.indexOf(formatted);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    countStart, countStart + formatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvLikes.setText(spannable);
        }

        private void buildCaption(Post post) {
            String username = post.getUser().getUsername();
            String full = username + " " + post.getCaption();
            SpannableString spannable = new SpannableString(full);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvCaption.setText(spannable);
        }

        private void buildDotIndicators(int count) {
            llDotIndicators.removeAllViews();
            int sizeDp = 6;
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            int sizePx = (int) (sizeDp * density);
            int marginPx = (int) (3 * density);

            for (int i = 0; i < count; i++) {
                View dot = new View(itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
                params.setMargins(marginPx, 0, marginPx, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(i == 0 ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
                llDotIndicators.addView(dot);
            }
        }

        private String formatCount(int count) {
            if (count >= 1_000_000) return String.format(Locale.getDefault(), "%.1fM", count / 1_000_000.0);
            if (count >= 1_000) return String.format(Locale.getDefault(), "%.1fK", count / 1_000.0)
                    .replace(".0K", "K");
            return String.valueOf(count);
        }
    }
}
