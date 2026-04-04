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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommentsBottomSheet extends BottomSheetDialogFragment {

    private final List<CommentModel> comments = new ArrayList<>();
    private CommentsAdapter adapter;

    public static CommentsBottomSheet newInstance() {
        return new CommentsBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments_sheet, container, false);

        RecyclerView rvComments = view.findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentsAdapter(comments);
        rvComments.setAdapter(adapter);

        loadMocks();

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
            if (!text.isEmpty()) {
                comments.add(0, new CommentModel("jacob_w", text, "vừa xong", 0, "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400"));
                adapter.notifyItemInserted(0);
                rvComments.scrollToPosition(0);
                etInput.setText("");
            }
        });

        return view;
    }

    private void loadMocks() {
        comments.add(new CommentModel("mythy.ne", "Bài học ở đây là gì", "17 giờ", 4, "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400"));
        comments.add(new CommentModel("_namxilam_", "=))))))", "21 giờ", 0, "https://images.unsplash.com/photo-1503023345310-bd7428a21914?w=400"));
        comments.add(new CommentModel("ei.milye", "=)))", "15 giờ", 0, "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"));
        comments.add(new CommentModel("kb.setalihm.na", "ê:)(", "20 giờ", 0, "https://images.unsplash.com/photo-1490750967868-88df5691cc9d?w=400"));
        comments.add(new CommentModel("imariceist", "khu 13 hay khu 18 vậy", "16 giờ", 2, "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=400"));
        adapter.notifyDataSetChanged();
    }
}
