package com.example.social_network.Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.social_network.R;
import com.example.social_network.View.StoryViewerView;

public class StoryViewerActivity extends AppCompatActivity {

    public static final String EXTRA_MEDIA_URL = "extra_media_url";
    public static final String EXTRA_AVATAR_URL = "extra_avatar_url";
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_TIME = "extra_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_viewer);

        String mediaUrl = getIntent().getStringExtra(EXTRA_MEDIA_URL);
        String avatarUrl = getIntent().getStringExtra(EXTRA_AVATAR_URL);
        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        String timeAgo = getIntent().getStringExtra(EXTRA_TIME);

        StoryViewerView storyViewer = findViewById(R.id.storyViewer);
        storyViewer.bind(mediaUrl, avatarUrl, username, timeAgo);
        storyViewer.setProgressState(3, 0);
        storyViewer.setListener(new StoryViewerView.Listener() {
            @Override
            public void onClose() {
                finish();
            }

            @Override
            public void onMore() {
                Toast.makeText(StoryViewerActivity.this, "More story options", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendMessage(String message) {
                if (message.isEmpty()) return;
                Toast.makeText(StoryViewerActivity.this, "Sent: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

