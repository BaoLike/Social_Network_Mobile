package com.example.social_network.Network;

import android.content.Context;

import com.example.social_network.Model.ConservationDTO;
import com.example.social_network.Model.ConversationRequestDto;
import com.example.social_network.Model.ConversationResponseDto;
import com.example.social_network.Model.FollowingUserDto;
import com.example.social_network.Model.MessageDto;
import com.example.social_network.Model.SendMessageRequestDto;

import java.util.List;

public interface ChatApiInterface {
    interface FollowingUsersCallback {
        void onSuccess(List<ConservationDTO> users);
        void onFailure(String message);
    }

    interface ConversationCallback {
        void onSuccess(ConversationResponseDto conversation);
        void onFailure(String message);
    }

    interface MessagesCallback {
        void onSuccess(List<MessageDto> messages);
        void onFailure(String message);
    }

    interface SendMessageCallback {
        void onSuccess();
        void onFailure(String message);
    }

    String getCurrentUserId(Context context);

    void getConservation(Context context, FollowingUsersCallback callback);

    /** GET /profile/getFollowed on profile service (JWT Bearer). */
    void getFollowedProfiles(Context context, FollowingUsersCallback callback);

    void getOrCreateConversation(Context context,
                                 ConversationRequestDto request,
                                 ConversationCallback callback);

    void getMessages(Context context, String conversationId, MessagesCallback callback);

    void postMessage(Context context, SendMessageRequestDto request, SendMessageCallback callback);
}
