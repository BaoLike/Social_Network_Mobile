package com.example.social_network;

import android.app.Application;

import com.example.social_network.Utils.ThemeManager;

public class SocialNetworkApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applyTheme(this);
    }
}
