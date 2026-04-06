package com.example.social_network.Activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.social_network.Model.SearchUserResult;
import com.example.social_network.Network.ChatApiService;
import com.example.social_network.Network.ProfileApiService;
import com.example.social_network.R;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

public class OtherUserProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER = "extra_search_user";

    private final ProfileApiService profileApi = new ProfileApiService();
    private MaterialButton btnFollow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        SearchUserResult user = readUserExtra();
        if (user == null) {
            Toast.makeText(this, "Không có dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btnOtherProfileBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView ivAvatar = findViewById(R.id.ivOtherUserAvatar);
        TextView tvUsername = findViewById(R.id.tvOtherUserUsername);
        TextView tvFullName = findViewById(R.id.tvOtherUserFullName);
        TextView tvDetail = findViewById(R.id.tvOtherUserDetail);
        btnFollow = findViewById(R.id.btnFollowUser);

        tvUsername.setText(user.getDisplayName());
        String full = user.getFullName();
        tvFullName.setText(full.isEmpty() ? user.getUserName() != null ? "@" + user.getUserName() : "" : full);

        StringBuilder detail = new StringBuilder();
        if (user.getGender() != null && !user.getGender().isEmpty()) {
            detail.append(user.getGender());
        }
        if (user.getDob() != null && !user.getDob().isEmpty()) {
            if (detail.length() > 0) {
                detail.append(" · ");
            }
            detail.append(user.getDob());
        }
        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            if (detail.length() > 0) {
                detail.append("\n");
            }
            detail.append(user.getAddress());
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            if (detail.length() > 0) {
                detail.append("\n");
            }
            detail.append(user.getPhone());
        }
        tvDetail.setText(detail.length() > 0 ? detail.toString() : "");

        String av = user.getAvatar();
        if (av != null && !av.isEmpty()) {
            Picasso.get().load(av).centerCrop().fit().into(ivAvatar);
        }

        String myId = new ChatApiService().getCurrentUserId(this);
        if (myId != null && myId.equals(user.getUserId())) {
            btnFollow.setEnabled(false);
            btnFollow.setText("Đây là bạn");
        } else {
            btnFollow.setOnClickListener(v -> sendFollow(user.getUserId()));
        }
    }

    @SuppressWarnings("deprecation")
    private SearchUserResult readUserExtra() {
        return (SearchUserResult) getIntent().getSerializableExtra(EXTRA_USER);
    }

    private void sendFollow(String userId) {
        btnFollow.setEnabled(false);
        profileApi.followUser(this, userId, new ProfileApiService.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(OtherUserProfileActivity.this, "Đã theo dõi", Toast.LENGTH_SHORT).show();
                    btnFollow.setText("Đang theo dõi");
                    btnFollow.setEnabled(false);
                });
            }

            @Override
            public void onFailure(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(OtherUserProfileActivity.this, message, Toast.LENGTH_LONG).show();
                    btnFollow.setEnabled(true);
                });
            }
        });
    }
}
