package com.example.social_network.Activity;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.social_network.Fragment.ActivityFragment;
import com.example.social_network.Fragment.AddFragment;
import com.example.social_network.Fragment.HomeFragment;
import com.example.social_network.Fragment.ProfileFragment;
import com.example.social_network.Fragment.SearchFragment;
import com.example.social_network.R;

public class HomeActivity extends AppCompatActivity {

    private static final int TAB_HOME     = 0;
    private static final int TAB_SEARCH   = 1;
    private static final int TAB_ADD      = 2;
    private static final int TAB_ACTIVITY = 3;
    private static final int TAB_PROFILE  = 4;

    private int currentTab = TAB_HOME;

    private ImageButton btnNavHome;
    private ImageButton btnNavSearch;
    private ImageButton btnNavAdd;
    private ImageButton btnNavHeart;
    private ImageButton btnNavProfile;

    // Keep fragment instances alive to preserve state when switching tabs
    private final HomeFragment     homeFragment     = HomeFragment.newInstance();
    private final SearchFragment   searchFragment   = SearchFragment.newInstance();
    private final AddFragment      addFragment      = AddFragment.newInstance();
    private final ActivityFragment activityFragment = ActivityFragment.newInstance();
    private final ProfileFragment  profileFragment  = ProfileFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bindViews();
        setupBottomNav();

        if (savedInstanceState == null) {
            showFragment(homeFragment, TAB_HOME);
        }
    }

    private void bindViews() {
        btnNavHome    = findViewById(R.id.btnNavHome);
        btnNavSearch  = findViewById(R.id.btnNavSearch);
        btnNavAdd     = findViewById(R.id.btnNavAdd);
        btnNavHeart   = findViewById(R.id.btnNavHeart);
        btnNavProfile = findViewById(R.id.btnNavProfile);
    }

    private void setupBottomNav() {
        btnNavHome.setOnClickListener(v    -> showFragment(homeFragment,     TAB_HOME));
        btnNavSearch.setOnClickListener(v  -> showFragment(searchFragment,   TAB_SEARCH));
        btnNavAdd.setOnClickListener(v     -> showFragment(addFragment,      TAB_ADD));
        btnNavHeart.setOnClickListener(v   -> showFragment(activityFragment, TAB_ACTIVITY));
        btnNavProfile.setOnClickListener(v -> showFragment(profileFragment,  TAB_PROFILE));
    }

    private void showFragment(Fragment fragment, int tab) {
        if (currentTab == tab && fragment.isAdded()) return;
        currentTab = tab;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Hide all currently added fragments
        for (Fragment f : fm.getFragments()) {
            ft.hide(f);
        }

        // Add or show target fragment
        if (!fragment.isAdded()) {
            ft.add(R.id.fragmentContainer, fragment);
        } else {
            ft.show(fragment);
        }

        ft.commit();
        updateNavTint(tab);
    }

    private void updateNavTint(int activeTab) {
        int colorActive   = ContextCompat.getColor(this, R.color.instagram_blue);
        int colorInactive = ContextCompat.getColor(this, R.color.colorTextSecondary);

        setIconTint(btnNavHome,    activeTab == TAB_HOME     ? colorActive : colorInactive);
        setIconTint(btnNavSearch,  activeTab == TAB_SEARCH   ? colorActive : colorInactive);
        setIconTint(btnNavAdd,     activeTab == TAB_ADD      ? colorActive : colorInactive);
        setIconTint(btnNavHeart,   activeTab == TAB_ACTIVITY ? colorActive : colorInactive);
        setIconTint(btnNavProfile, activeTab == TAB_PROFILE  ? colorActive : colorInactive);
    }

    private void setIconTint(ImageButton btn, int color) {
        btn.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
    }
}
