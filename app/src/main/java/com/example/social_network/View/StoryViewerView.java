package com.example.social_network.View;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.social_network.R;
import com.squareup.picasso.Picasso;

/**
 * Reusable story viewer component.
 * Can be used inside any Activity/Fragment layout.
 */
public class StoryViewerView extends FrameLayout {

    public interface Listener {
        void onClose();
        void onMore();
        void onSendMessage(String message);
    }

    private LinearLayout progressContainer;
    private ImageView ivStoryMedia;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvTimeAgo;
    private TextView tvClose;
    private ImageView ivMore;
    private EditText etMessage;
    private ImageView ivSend;
    private Listener listener;

    public StoryViewerView(Context context) {
        super(context);
        init();
    }

    public StoryViewerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StoryViewerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_story_viewer, this, true);
        progressContainer = findViewById(R.id.layoutStoryProgress);
        ivStoryMedia = findViewById(R.id.ivStoryMedia);
        ivAvatar = findViewById(R.id.ivStoryAvatar);
        tvUsername = findViewById(R.id.tvStoryUsername);
        tvTimeAgo = findViewById(R.id.tvStoryTimeAgo);
        tvClose = findViewById(R.id.tvStoryClose);
        ivMore = findViewById(R.id.ivStoryMore);
        etMessage = findViewById(R.id.etStoryMessage);
        ivSend = findViewById(R.id.ivStorySend);

        tvClose.setOnClickListener(v -> {
            if (listener != null) listener.onClose();
        });
        ivMore.setOnClickListener(v -> {
            if (listener != null) listener.onMore();
        });
        ivSend.setOnClickListener(v -> {
            if (listener == null) return;
            String text = etMessage.getText().toString().trim();
            listener.onSendMessage(text);
            etMessage.setText("");
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void bind(String mediaUrl, String avatarUrl, String username, String timeAgo) {
        tvUsername.setText(TextUtils.isEmpty(username) ? "unknown_user" : username);
        tvTimeAgo.setText(TextUtils.isEmpty(timeAgo) ? "now" : timeAgo);

        if (!TextUtils.isEmpty(mediaUrl)) {
            Picasso.get().load(mediaUrl).centerCrop().fit().into(ivStoryMedia);
        } else {
            ivStoryMedia.setImageResource(R.drawable.story_ring_gray);
        }

        if (!TextUtils.isEmpty(avatarUrl)) {
            Picasso.get().load(avatarUrl).centerCrop().fit().into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_nav_profile);
        }
    }

    /**
     * Draw story progress bars at top.
     */
    public void setProgressState(int totalSegments, int activeSegment) {
        progressContainer.removeAllViews();
        int safeTotal = Math.max(1, totalSegments);
        int safeActive = Math.min(Math.max(0, activeSegment), safeTotal - 1);
        for (int i = 0; i < safeTotal; i++) {
            View bar = new View(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(2), 1f);
            if (i > 0) lp.setMarginStart(dp(4));
            bar.setLayoutParams(lp);
            bar.setBackgroundColor(i <= safeActive ? 0xFFFFFFFF : 0x66FFFFFF);
            progressContainer.addView(bar);
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (value * density);
    }
}

