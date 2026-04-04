package com.example.social_network.Fragment;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Adapter.MessagesAdapter;
import com.example.social_network.Model.ConservationDTO;
import com.example.social_network.Model.ConversationModel;
import com.example.social_network.Network.ChatApiInterface;
import com.example.social_network.Network.ChatApiService;
import com.example.social_network.R;
import com.example.social_network.Activity.ChatDetailActivity;
import com.example.social_network.Utils.TokenManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment that displays the Instagram-style Direct Messages screen.
 * Replaces the old "Activity & Notifications" placeholder.
 */
public class ActivityFragment extends Fragment {
    private final List<ConversationModel> conversations = new ArrayList<>();
    private final List<ConversationModel> followedCache = new ArrayList<>();
    private MessagesAdapter messagesAdapter;
    private ChatApiService chatApiService;
    private EditText etMsgSearch;

    public static ActivityFragment newInstance() {
        return new ActivityFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        RecyclerView rv = view.findViewById(R.id.rvConversations);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        messagesAdapter = new MessagesAdapter(conversations, conversation -> {
            Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
            intent.putExtra(ChatDetailActivity.EXTRA_USERNAME, conversation.getUsername());
            intent.putExtra(ChatDetailActivity.EXTRA_RECEIVER_ID, conversation.getReceiverId());
            if (conversation.getConversationId() != null && !conversation.getConversationId().isEmpty()) {
                intent.putExtra(ChatDetailActivity.EXTRA_CONVERSATION_ID, conversation.getConversationId());
            }
            startActivity(intent);
        });
        rv.setAdapter(messagesAdapter);

        chatApiService = new ChatApiService();
        fetchFollowingUsers();

        etMsgSearch = view.findViewById(R.id.etMsgSearch);
        etMsgSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                fetchFollowedUsersForSearch();
            } else if (etMsgSearch.getText().toString().trim().isEmpty()) {
                fetchFollowingUsers();
            }
        });
        etMsgSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!followedCache.isEmpty() && etMsgSearch.hasFocus()) {
                    applyFollowedFilter(s.toString());
                }
            }
        });

        return view;
    }

    private void applyFollowedFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        conversations.clear();
        for (ConversationModel m : followedCache) {
            if (q.isEmpty() || m.getUsername().toLowerCase(Locale.getDefault()).contains(q)) {
                conversations.add(m);
            }
        }
        messagesAdapter.notifyDataSetChanged();
    }

    private void fetchFollowedUsersForSearch() {
        if (!TokenManager.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        chatApiService.getFollowedProfiles(requireContext(), new ChatApiInterface.FollowingUsersCallback() {
            @Override
            public void onSuccess(List<ConservationDTO> users) {
                followedCache.clear();
                for (ConservationDTO user : users) {
                    String receiverId = user.getUserId() != null ? user.getUserId() : "";
                    String label = user.getUserName() != null ? user.getUserName() : receiverId;
                    followedCache.add(new ConversationModel(
                            receiverId,
                            label,
                            "",
                            "",
                            Color.parseColor("#3797EF"),
                            null
                    ));
                }
                applyFollowedFilter(etMsgSearch.getText().toString());
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFollowingUsers() {
        followedCache.clear();

        chatApiService.getConservation(requireContext(), new ChatApiInterface.FollowingUsersCallback() {
            @Override
            public void onSuccess(List<ConservationDTO> users) {
                conversations.clear();
                for (ConservationDTO user : users) {
                    String receiverId = user.getUserId() != null ? user.getUserId() : "";
                    String label = user.getUserName() != null ? user.getUserName() : receiverId;
                    String convId = user.getConversationId();
                    conversations.add(new ConversationModel(
                            receiverId,
                            label,
                            "",
                            "",
                            Color.parseColor("#3797EF"),
                            convId
                    ));
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            }
        });
    }
}
