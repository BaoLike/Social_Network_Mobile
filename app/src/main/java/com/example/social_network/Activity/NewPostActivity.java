package com.example.social_network.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.social_network.R;
import com.example.social_network.Utils.FetchApi;
import com.squareup.picasso.Picasso;

/**
 * Second step of the "create post" flow.
 * Shows the chosen image thumbnail, a caption field, and Tag/Location options.
 * Pressing "Share" adds the new post to PostRepository and finishes the
 * entire create-flow (AddImageActivity + this Activity).
 */
public class NewPostActivity extends AppCompatActivity {

    private static final String TAG = "NewPostActivity";
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        imageUrl = getIntent().getStringExtra(AddImageActivity.KEY_IMAGE_URL);
        if (imageUrl == null) imageUrl = "";

        // Thumbnail preview
        ImageView ivThumb = findViewById(R.id.ivNewPostThumb);
        if (!imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).centerCrop().fit().into(ivThumb);
        }

        // Back button
        findViewById(R.id.btnNewPostBack).setOnClickListener(v -> finish());

        // Share button → call create-post API (multipart: media + data)
        EditText etCaption = findViewById(R.id.etCaption);
        TextView tvShare = findViewById(R.id.tvShare);
        tvShare.setOnClickListener(v -> {
            String caption = etCaption.getText().toString().trim();
            Uri mediaUri = parseUploadableMediaUri(imageUrl);
            if (mediaUri == null) {
                Toast.makeText(this,
                        "Ảnh mẫu không upload được. Vui lòng chọn ảnh/video từ thiết bị.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            tvShare.setEnabled(false);
            tvShare.setText("Sharing...");
            Toast.makeText(this, "Đang đăng bài...", Toast.LENGTH_SHORT).show();
            new FetchApi().postCreatePost(getApplicationContext(), mediaUri, caption, TAG, new FetchApi.ApiCallback() {
                @Override
                public void onSuccess() {
                    // No-op: optimistic navigation already happened.
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(getApplicationContext(),
                            "Đăng bài nền thất bại: " + message, Toast.LENGTH_LONG).show();
                }
            });

            Intent intent = new Intent(NewPostActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private Uri parseUploadableMediaUri(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        Uri uri = Uri.parse(value);
        if (uri == null) return null;
        String scheme = uri.getScheme();
        if ("file".equalsIgnoreCase(scheme) || "content".equalsIgnoreCase(scheme)) {
            return uri;
        }
        return null;
    }
}
