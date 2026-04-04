package com.example.social_network.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Model.CommentModel;
import com.example.social_network.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final List<CommentModel> comments;

    public CommentsAdapter(List<CommentModel> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = comments.get(position);

        holder.tvUsername.setText(comment.getUsername());
        holder.tvText.setText(comment.getCommentText());
        holder.tvTime.setText(comment.getTimeAgo());
        holder.tvLikes.setText(String.valueOf(comment.getLikeCount()));

        if (comment.getAvatarUrl() != null && !comment.getAvatarUrl().isEmpty()) {
            Picasso.get().load(comment.getAvatarUrl()).centerCrop().fit().into(holder.ivAvatar);
        } else {
            Picasso.get().load("https://images.unsplash.com/photo-1542272604-787c3835535d?w=400").centerCrop().fit().into(holder.ivAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername, tvTime, tvText, tvLikes;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivCommentAvatar);
            tvUsername = itemView.findViewById(R.id.tvCommentUsername);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            tvText = itemView.findViewById(R.id.tvCommentText);
            tvLikes = itemView.findViewById(R.id.tvCommentLikes);
        }
    }
}
