package com.example.social_network;

import androidx.annotation.NonNull;

import com.example.social_network.Utils.FcmRegistrationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * FCM: khi token đổi, đăng ký lại lên notification service.
 */
public class SocialNetworkFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        FcmRegistrationHelper.register(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Hiển thị notification tùy payload — có thể mở rộng sau
    }
}
