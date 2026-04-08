package com.example.social_network.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.social_network.Data.PostRepository;
import com.example.social_network.Model.PostGridModel;
import com.example.social_network.R;
import com.squareup.picasso.Picasso;

/**
 * Full-screen post detail — opened when the user taps a cell in the Posts grid.
 * Pass the post position via Intent extra KEY_POSITION.
 */
public class PostDetailActivity extends AppCompatActivity {

    public static final String KEY_POSITION = "post_position";

    private PostGridModel post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        int position = getIntent().getIntExtra(KEY_POSITION, 0);
        post = PostRepository.getInstance().getPosts().get(position);

        bindViews();
    }

    private void bindViews() {
        // ── Back button ───────────────────────────────────────────────────
        ImageButton btnBack = findViewById(R.id.btnDetailBack);
        btnBack.setOnClickListener(v -> finish());

        // ── Image ─────────────────────────────────────────────────────────
        ImageView ivImage = findViewById(R.id.ivDetailImage);
        Picasso.get().load(post.getImageUrl()).centerCrop().fit().into(ivImage);

        // ── Avatar (colour circle) ─────────────────────────────────────────
        // For simplicity, just load the same image scaled down as the avatar.
        ImageView ivAvatar = findViewById(R.id.ivDetailAvatar);
        Picasso.get().load(post.getImageUrl()).centerCrop().fit().into(ivAvatar);

        // ── Username ──────────────────────────────────────────────────────
        ((TextView) findViewById(R.id.tvDetailUsername)).setText(post.getUsername());

        // ── Likes ─────────────────────────────────────────────────────────
        updateLikeCount();

        // ── Caption ───────────────────────────────────────────────────────
        TextView tvCaption = findViewById(R.id.tvDetailCaption);
        String capText = post.getCaption().isEmpty()
                ? ""
                : post.getUsername() + "  " + post.getCaption();
        tvCaption.setText(capText);

        // ── Time ──────────────────────────────────────────────────────────
        ((TextView) findViewById(R.id.tvDetailTime)).setText(post.getTimeAgo());

        // ── Like button (toggle) ──────────────────────────────────────────
        ImageButton btnLike = findViewById(R.id.btnDetailLike);
        refreshLikeIcon(btnLike);
        btnLike.setOnClickListener(v -> {
            post.setLiked(!post.isLiked());
            post.setLikeCount(post.getLikeCount() + (post.isLiked() ? 1 : -1));
            refreshLikeIcon(btnLike);
            updateLikeCount();
        });

        // ── Bookmark button (toggle) ───────────────────────────────────────
        ImageButton btnBookmark = findViewById(R.id.btnDetailBookmark);
        refreshBookmarkIcon(btnBookmark);
        btnBookmark.setOnClickListener(v -> {
            post.setBookmarked(!post.isBookmarked());
            refreshBookmarkIcon(btnBookmark);
        });

        // ── Comment / Share (stub) ─────────────────────────────────────────
        findViewById(R.id.btnDetailComment).setOnClickListener(v -> {
            com.example.social_network.Fragment.CommentsBottomSheet sheet =
                    com.example.social_network.Fragment.CommentsBottomSheet.newInstance(new java.util.ArrayList<>());
            sheet.show(getSupportFragmentManager(), "CommentsBottomSheet");
        });

        ImageView ivCommentAvatar = findViewById(R.id.ivCommentAvatar);
        Picasso.get().load(post.getImageUrl()).centerCrop().fit().into(ivCommentAvatar);

        // Show/hide "Post" text button while typing a comment
        EditText etComment = findViewById(R.id.etAddComment);
        TextView tvPost    = findViewById(R.id.tvPost);
        etComment.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                tvPost.setVisibility(s.length() > 0
                        ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void updateLikeCount() {
        TextView tvLikes = findViewById(R.id.tvDetailLikes);
        tvLikes.setText(post.getLikeCount() + " likes");
    }

    private void refreshLikeIcon(ImageButton btn) {
        if (post.isLiked()) {
            btn.setImageResource(R.drawable.ic_heart_filled);
            btn.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light));
        } else {
            btn.setImageResource(R.drawable.ic_heart_outline);
            btn.setColorFilter(ContextCompat.getColor(this, R.color.colorTextPrimary));
        }
    }

    private void refreshBookmarkIcon(ImageButton btn) {
        btn.setImageResource(post.isBookmarked()
                ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
    }
}
