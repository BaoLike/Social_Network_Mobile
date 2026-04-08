package com.example.social_network.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Adapter.CommentsAdapter;
import com.example.social_network.Model.CommentModel;
import com.example.social_network.R;
import com.example.social_network.Utils.FetchApi;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommentsBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_COMMENTS = "arg_comments";
    private static final String ARG_POST_ID = "arg_post_id";
    private static final String TAG = "CommentsBottomSheet";

    private final List<CommentModel> comments = new ArrayList<>();
    private CommentsAdapter adapter;
    private String postId;

    public static CommentsBottomSheet newInstance(List<CommentModel> inputComments) {
        return newInstance(null, inputComments);
    }

    public static CommentsBottomSheet newInstance(String postId, List<CommentModel> inputComments) {
        CommentsBottomSheet sheet = new CommentsBottomSheet();
        Bundle args = new Bundle();
        ArrayList<CommentModel> serializableComments = new ArrayList<>();
        if (inputComments != null) {
            serializableComments.addAll(inputComments);
        }
        args.putSerializable(ARG_COMMENTS, serializableComments);
        args.putString(ARG_POST_ID, postId);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments_sheet, container, false);

        RecyclerView rvComments = view.findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentsAdapter(comments);
        rvComments.setAdapter(adapter);

        loadFromArguments();

        // Input
        EditText etInput = view.findViewById(R.id.etCommentInput);
        TextView tvSend = view.findViewById(R.id.tvSendComment);
        ImageView ivUserAvatar = view.findViewById(R.id.ivCommentUserAvatar);

        Picasso.get().load("https://images.unsplash.com/photo-1542272604-787c3835535d?w=400").centerCrop().fit().into(ivUserAvatar);

        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    tvSend.setVisibility(View.VISIBLE);
                } else {
                    tvSend.setVisibility(View.GONE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        tvSend.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (text.isEmpty()) return;
            // Optimistic comment: update UI immediately, request in background.
            comments.add(0, new CommentModel("you", text, "vừa xong", 0,
                    "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400"));
            adapter.notifyItemInserted(0);
            rvComments.scrollToPosition(0);
            etInput.setText("");

            new FetchApi().putCommentToPost(requireContext(), postId, text, TAG, new FetchApi.ApiCallback() {
                @Override
                public void onSuccess() {
                    // No-op: UI already updated.
                }

                @Override
                public void onFailure(String message) {
                    android.widget.Toast.makeText(requireContext(),
                            "Gửi comment nền thất bại: " + message,
                            android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    @SuppressWarnings("unchecked")
    private void loadFromArguments() {
        Bundle args = getArguments();
        if (args == null) return;
        postId = args.getString(ARG_POST_ID, null);
        Object raw = args.getSerializable(ARG_COMMENTS);
        if (raw instanceof ArrayList) {
            comments.clear();
            comments.addAll((ArrayList<CommentModel>) raw);
        }
        adapter.notifyDataSetChanged();
    }
}
