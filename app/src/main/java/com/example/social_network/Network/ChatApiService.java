package com.example.social_network.Network;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.social_network.Config.AppConfig;
import com.example.social_network.Model.ConservationDTO;
import com.example.social_network.Model.ConversationRequestDto;
import com.example.social_network.Model.ConversationResponseDto;
import com.example.social_network.Model.FollowingUserDto;
import com.example.social_network.Model.MessageDto;
import com.example.social_network.Model.SendMessageRequestDto;
import com.example.social_network.Utils.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatApiService implements ChatApiInterface {
    private static final String PROFILE_FOLLOWING_PATH = "/profile/getFollowed";
    private static final String CONVERSATIONS_PATH = "/my-conversations";
    private static final String MESSAGES_PATH = "/messages";

    @Override
    public String getCurrentUserId(Context context) {
        String token = TokenManager.getAccessToken(context);
        if (token == null || token.isEmpty()) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            String payload = new String(decodedBytes, StandardCharsets.UTF_8);
            JSONObject jsonPayload = new JSONObject(payload);
            return jsonPayload.optString("sub", null);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void getConservation(Context context, FollowingUsersCallback callback) {
        String url = AppConfig.BASE_URL_CONSERVATION + CONVERSATIONS_PATH;
        Log.e("url request", url);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onSuccess(parseFollowingUsers(response)),
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    @Override
    public void getOrCreateConversation(Context context,
                                        ConversationRequestDto requestDto,
                                        ConversationCallback callback) {
        String url = AppConfig.BSSE_URL_CHAT + CONVERSATIONS_PATH;
        JSONObject body = new JSONObject();
        try {
            body.put("senderId", requestDto.getSenderId());
            body.put("receiverId", requestDto.getReceiverId());
        } catch (JSONException e) {
            callback.onFailure("Invalid request body");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> callback.onSuccess(parseConversationResponse(response.optJSONObject("result"))),
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    @Override
    public void getMessages(Context context, String conversationId, MessagesCallback callback) {
        String url = AppConfig.BSSE_URL_CHAT + MESSAGES_PATH + "/" + conversationId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> callback.onSuccess(parseMessages(response.optJSONArray("result"))),
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    @Override
    public void postMessage(Context context, SendMessageRequestDto requestDto, SendMessageCallback callback) {
        String url = AppConfig.BSSE_URL_CHAT + MESSAGES_PATH;
        JSONObject body = new JSONObject();
        try {
            body.put("conversationId", requestDto.getConversationId());
            body.put("senderId", requestDto.getSenderId());
            body.put("content", requestDto.getContent());
        } catch (JSONException e) {
            callback.onFailure("Invalid request body");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> callback.onSuccess(),
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    private RequestQueue newQueue(Context context) {
        return Volley.newRequestQueue(context.getApplicationContext());
    }

    private Map<String, String> buildAuthHeaders(Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        String accessToken = TokenManager.getAccessToken(context);
        if (accessToken != null) {
            headers.put("Authorization", "Bearer " + accessToken);
        }
        return headers;
    }

    private List<ConservationDTO> parseFollowingUsers(JSONObject response) {
        List<ConservationDTO> conservationDTOList = new ArrayList<>();
        if (response == null) {
            return conservationDTOList;
        }

        JSONArray listFollower = response.optJSONArray("result");
        if (listFollower == null) {
            return conservationDTOList;
        }

        for (int i = 0; i < listFollower.length(); i++) {
            JSONObject item = listFollower.optJSONObject(i);
            if (item == null) {
                continue;
            }

            JSONArray participantsResponse = item.optJSONArray("participants");
            if (participantsResponse == null || participantsResponse.length() == 0) {
                continue;
            }

            JSONObject participant = participantsResponse.optJSONObject(0);
            if (participant == null) {
                continue;
            }

            String peerUserId = participant.optString("userId", null);
            String conversationAvatar = participant.optString("avatar", null);
            String userName = participant.optString("userName", null);
            if (userName == null || userName.isEmpty()) {
                userName = peerUserId;
            }

            if (userName != null && !userName.isEmpty()) {
                conservationDTOList.add(new ConservationDTO(peerUserId, conversationAvatar, userName));
            }
        }

        return conservationDTOList;
    }

    @Override
    public void getFollowedProfiles(Context context, FollowingUsersCallback callback) {
        String url = AppConfig.BSSE_URL_PROFILE + PROFILE_FOLLOWING_PATH;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (!"1000".equals(String.valueOf(response.opt("code")))) {
                        callback.onFailure(response.optString("message", "Request failed"));
                        return;
                    }
                    callback.onSuccess(parseFollowedProfileUsers(response));
                },
                error -> callback.onFailure(parseError(error))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return buildAuthHeaders(context);
            }
        };

        newQueue(context).add(request);
    }

    private List<ConservationDTO> parseFollowedProfileUsers(JSONObject response) {
        List<ConservationDTO> list = new ArrayList<>();
        if (response == null) {
            return list;
        }

        JSONArray arr = response.optJSONArray("result");
        if (arr == null) {
            return list;
        }

        for (int i = 0; i < arr.length(); i++) {
            JSONObject item = arr.optJSONObject(i);
            if (item == null) {
                continue;
            }

            String userId = item.optString("userId", "");
            String userName = item.isNull("userName") ? null : item.optString("userName", null);
            String firstName = item.optString("firstName", "");
            String lastName = item.optString("lastName", "");
            String display = (userName != null && !userName.isEmpty())
                    ? userName
                    : (firstName + " " + lastName).trim();
            if (display.isEmpty()) {
                display = userId;
            }

            String avatar = item.isNull("avatar") ? null : item.optString("avatar", null);
            list.add(new ConservationDTO(userId, avatar, display));
        }

        return list;
    }

    private ConversationResponseDto parseConversationResponse(JSONObject response) {
        if (response == null) {
            return new ConversationResponseDto(null);
        }
        String conversationId = response.optString("conversationId", null);
        return new ConversationResponseDto(conversationId);
    }

    private List<MessageDto> parseMessages(JSONArray response) {
        List<MessageDto> messages = new ArrayList<>();
        if (response == null) {
            return messages;
        }
        for (int i = 0; i < response.length(); i++) {
            JSONObject item = response.optJSONObject(i);
            if (item == null) {
                continue;
            }

            String id = item.optString("id", null);
            String conversationId = item.optString("conversationId", null);
            String senderId = item.optString("senderId", null);
            String content = item.optString("content", null);
            String timestamp = item.optString("timestamp", null);

            messages.add(new MessageDto(id, conversationId, senderId, content, timestamp));
        }
        return messages;
    }

    private String parseError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data, StandardCharsets.UTF_8);
        }
        return "Network request failed";
    }
}
