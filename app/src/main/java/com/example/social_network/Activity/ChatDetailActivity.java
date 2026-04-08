package com.example.social_network.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Adapter.ChatMessagesAdapter;
import com.example.social_network.Model.ChatMessage;
import com.example.social_network.Model.ConversationRequestDto;
import com.example.social_network.Model.MessageDto;
import com.example.social_network.Model.SendMessageRequestDto;
import com.example.social_network.Network.ChatApiInterface;
import com.example.social_network.Network.ChatApiService;
import com.example.social_network.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_RECEIVER_ID = "extra_receiver_id";
    /** When set (e.g. from inbox row), load messages without calling create again. */
    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";

    private RecyclerView rvMessages;
    private ChatMessagesAdapter adapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatApiService chatApiService;
    private String currentUserId;
    private String receiverId;
    private String conversationId;

    private EditText etMessageInput;
    private ImageView btnSendText;
    private LinearLayout llInputActions;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Quyền truy cập bị từ chối.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Header info
        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        receiverId = getIntent().getStringExtra(EXTRA_RECEIVER_ID);
        if (username == null) username = receiverId;

        TextView tvTitle = findViewById(R.id.tvChatTitle);
        tvTitle.setText(username);
        
        ImageView ivAvatar = findViewById(R.id.ivChatAvatar);
        Picasso.get().load("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400").centerCrop().fit().into(ivAvatar);

        findViewById(R.id.btnChatBack).setOnClickListener(v -> finish());

        // Recycler setup
        rvMessages = findViewById(R.id.rvMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setStackFromEnd(true); // helps start from bottom
        rvMessages.setLayoutManager(layoutManager);
        
        adapter = new ChatMessagesAdapter(messages);
        rvMessages.setAdapter(adapter);

        chatApiService = new ChatApiService();
        currentUserId = chatApiService.getCurrentUserId(this);
        loadConversationAndMessages();

        // Input views
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendText = findViewById(R.id.btnSendText);
        llInputActions = findViewById(R.id.llInputActions);

        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    btnSendText.setVisibility(View.VISIBLE);
                    llInputActions.setVisibility(View.GONE);
                } else {
                    btnSendText.setVisibility(View.GONE);
                    llInputActions.setVisibility(View.VISIBLE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Send Text Action
        btnSendText.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessageInput.setText(""); // clear input
            }
        });

        // Gallery Action
        ImageView btnGallery = findViewById(R.id.btnGalleryImage);
        btnGallery.setOnClickListener(v -> checkPermissionAndPickImage());
    }

    private void loadConversationAndMessages() {
        String presetConversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        if (presetConversationId != null && !presetConversationId.trim().isEmpty()) {
            conversationId = presetConversationId.trim();
            loadMessages(conversationId);
            return;
        }

        if (currentUserId == null || receiverId == null) {
            Toast.makeText(this, "Cannot load chat data", Toast.LENGTH_SHORT).show();
            return;
        }

        ConversationRequestDto requestDto = new ConversationRequestDto(currentUserId, receiverId);
        chatApiService.getOrCreateConversation(this, requestDto, new ChatApiInterface.ConversationCallback() {
            @Override
            public void onSuccess(com.example.social_network.Model.ConversationResponseDto conversation) {
                conversationId = conversation.getConversationId();
                if (conversationId == null || conversationId.isEmpty()) {
                    Toast.makeText(ChatDetailActivity.this,
                            "Server không trả về id cuộc trò chuyện — không thể tải tin nhắn",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                loadMessages(conversationId);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ChatDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages(String conversationIdValue) {
        chatApiService.getMessages(this, conversationIdValue, new ChatApiInterface.MessagesCallback() {
            @Override
            public void onSuccess(List<MessageDto> messageDtos) {
                messageDtos.sort(Comparator.comparing(MessageDto::getTimestamp, Comparator.nullsLast(String::compareTo)));

                messages.clear();
                for (MessageDto dto : messageDtos) {
                    messages.add(new ChatMessage(
                            dto.getContent(),
                            null,
                            dto.getTimestamp(),
                            dto.isFromMe(),
                            dto.isFromMe() ? null : dto.getIncomingAvatarUrl()
                    ));
                }
                adapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    rvMessages.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ChatDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String content) {
        if (conversationId == null || currentUserId == null) {
            Toast.makeText(this, "Cannot send message", Toast.LENGTH_SHORT).show();
            return;
        }

        addMessage(new ChatMessage(content, null, "", true));

        SendMessageRequestDto requestDto = new SendMessageRequestDto(conversationId, content);

        chatApiService.postMessage(this, requestDto, new ChatApiInterface.SendMessageCallback() {
            @Override
            public void onSuccess(MessageDto sentMessage) {
                if (sentMessage == null || messages.isEmpty()) {
                    return;
                }
                int last = messages.size() - 1;
                ChatMessage lastMsg = messages.get(last);
                if (!lastMsg.isOutgoing()) {
                    return;
                }
                String ts = sentMessage.getTimestamp();
                if (ts == null || ts.isEmpty()) {
                    return;
                }
                messages.set(last, new ChatMessage(
                        sentMessage.getContent(),
                        null,
                        ts,
                        sentMessage.isFromMe(),
                        sentMessage.isFromMe() ? null : sentMessage.getIncomingAvatarUrl()
                ));
                adapter.notifyItemChanged(last);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ChatDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessage(ChatMessage message) {
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);
    }

    private void checkPermissionAndPickImage() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImageFromGallery();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void pickImageFromGallery() {
        // Grab the most recent image from the gallery as a quick "pick" for this mock
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String imagePath = cursor.getString(dataColumn);
                
                // Add the image to chat
                addMessage(new ChatMessage("", "file://" + imagePath, "vừa xong", true));
            } else {
                Toast.makeText(this, "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi truy xuất ảnh", Toast.LENGTH_SHORT).show();
        }
    }
}
