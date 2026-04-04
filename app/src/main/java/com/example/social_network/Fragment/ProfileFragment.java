package com.example.social_network.Fragment;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.social_network.Activity.MainActivity;
import com.example.social_network.R;
import com.example.social_network.Utils.FetchApi;
import com.example.social_network.Utils.ThemeManager;
import com.example.social_network.Utils.TokenManager;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

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

        ImageView ivAvatar = view.findViewById(R.id.ivProfileAvatar);
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(0xFF3F51B5);
        ivAvatar.setBackground(avatarBg);

        view.findViewById(R.id.listAction).setOnClickListener(this::showActionMenu);
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
                Toast.makeText(requireContext(), "Edit profile", Toast.LENGTH_SHORT).show();
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
