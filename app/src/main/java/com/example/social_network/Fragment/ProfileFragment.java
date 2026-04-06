package com.example.social_network.Fragment;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.social_network.Activity.EditProfileActivity;
import com.example.social_network.Activity.MainActivity;
import com.example.social_network.Model.UserProfile;
import com.example.social_network.Network.ProfileApiService;
import com.example.social_network.R;
import com.example.social_network.Utils.FetchApi;
import com.example.social_network.Utils.ThemeManager;
import com.example.social_network.Utils.TokenManager;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private TextView tvToolbarUsername;
    private TextView tvBioUsername;
    private TextView tvStatFollowers;
    private TextView tvStatFollowing;
    private ImageView ivProfileAvatar;
    private TextView tvBioDetail;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvToolbarUsername = view.findViewById(R.id.tvProfileToolbarUsername);
        tvBioUsername = view.findViewById(R.id.tvProfileBioUsername);
        tvStatFollowers = view.findViewById(R.id.tvStatFollowers);
        tvStatFollowing = view.findViewById(R.id.tvStatFollowing);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        tvBioDetail = view.findViewById(R.id.tvProfileBioDetail);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(0xFF3F51B5);
        ivProfileAvatar.setBackground(avatarBg);

        view.findViewById(R.id.listAction).setOnClickListener(this::showActionMenu);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyProfile();
    }

    private void loadMyProfile() {
        if (tvToolbarUsername == null || !TokenManager.isLoggedIn(requireContext())) {
            return;
        }

        new ProfileApiService().getMyProfile(requireContext(), new ProfileApiService.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                if (getView() == null) {
                    return;
                }
                String name = profile.getDisplayName();
                tvToolbarUsername.setText(name);
                tvBioUsername.setText(name);
                tvStatFollowers.setText(String.valueOf(profile.getFollower()));
                tvStatFollowing.setText(String.valueOf(profile.getFollowed()));

                String avatarUrl = profile.getAvatar();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Picasso.get().load(avatarUrl).centerCrop().fit().into(ivProfileAvatar);
                }

                String addr = profile.getAddress();
                String dob = profile.getDob();
                StringBuilder bio = new StringBuilder();
                if (dob != null && !dob.isEmpty()) {
                    bio.append(dob);
                }
                if (addr != null && !addr.isEmpty()) {
                    if (bio.length() > 0) {
                        bio.append(" · ");
                    }
                    bio.append(addr);
                }
                tvBioDetail.setText(bio.length() > 0 ? bio.toString() : "Chạm menu ⋮ để chỉnh sửa hồ sơ");
            }

            @Override
            public void onFailure(String message) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showActionMenu(View anchor) {
        boolean isDark = ThemeManager.isDarkMode(requireContext());

        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, R.id.menu_edit_profile,    0, "Edit profile");
        popup.getMenu().add(0, R.id.menu_change_password, 1, "Change password");
        popup.getMenu().add(0, R.id.menu_toggle_theme,    2, isDark ? "Switch to Light mode" : "Switch to Dark mode");
        popup.getMenu().add(0, R.id.menu_logout,          3, "Logout");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit_profile) {
                startActivity(new Intent(requireContext(), EditProfileActivity.class));
                return true;
            } else if (id == R.id.menu_change_password) {
                Toast.makeText(requireContext(), "Change password", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_toggle_theme) {
                ThemeManager.toggle(requireContext());
                return true;
            } else if (id == R.id.menu_logout) {
                handleLogout();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void handleLogout() {
        new FetchApi().postLogout(requireContext(), TAG, new FetchApi.ApiCallback() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
