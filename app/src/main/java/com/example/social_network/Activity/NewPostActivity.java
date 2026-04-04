package com.example.social_network.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.social_network.Data.PostRepository;
import com.example.social_network.Model.PostGridModel;
import com.example.social_network.R;
import com.squareup.picasso.Picasso;

/**
 * Second step of the "create post" flow.
 * Shows the chosen image thumbnail, a caption field, and Tag/Location options.
 * Pressing "Share" adds the new post to PostRepository and finishes the
 * entire create-flow (AddImageActivity + this Activity).
 */
public class NewPostActivity extends AppCompatActivity {

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

        // Share button → create PostGridModel, add to repo, finish entire flow
        EditText etCaption = findViewById(R.id.etCaption);
        findViewById(R.id.tvShare).setOnClickListener(v -> {
            String caption = etCaption.getText().toString().trim();

            PostGridModel newPost = new PostGridModel(
                    imageUrl,
                    PostGridModel.Type.IMAGE,
                    0,                       // 0 likes initially
                    "jacob_w",               // current user
                    caption.isEmpty() ? "✨" : caption,
                    "just now"
            );

            PostRepository.getInstance().addPost(newPost);

            Toast.makeText(this, "Post shared!", Toast.LENGTH_SHORT).show();

            // Close both NewPostActivity and AddImageActivity
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
