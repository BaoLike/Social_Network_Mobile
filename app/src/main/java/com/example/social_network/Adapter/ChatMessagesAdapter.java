package com.example.social_network.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Model.ChatMessage;
import com.example.social_network.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.MessageViewHolder> {

    private final List<ChatMessage> messages;

    public ChatMessagesAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        // Reset visibility
        holder.llOutgoing.setVisibility(View.GONE);
        holder.llIncoming.setVisibility(View.GONE);
        holder.tvOutgoingText.setVisibility(View.GONE);
        holder.flOutgoingImage.setVisibility(View.GONE);
        holder.tvIncomingText.setVisibility(View.GONE);

        if (msg.isOutgoing()) {
            holder.llOutgoing.setVisibility(View.VISIBLE);

            if (msg.getImageUri() != null && !msg.getImageUri().isEmpty()) {
                holder.flOutgoingImage.setVisibility(View.VISIBLE);
                Picasso.get().load(msg.getImageUri()).centerCrop().fit().into(holder.ivOutgoingImage);
            } else {
                holder.tvOutgoingText.setVisibility(View.VISIBLE);
                holder.tvOutgoingText.setText(msg.getText());
            }
        } else {
            holder.llIncoming.setVisibility(View.VISIBLE);
            holder.tvIncomingText.setVisibility(View.VISIBLE);
            holder.tvIncomingText.setText(msg.getText());

            String avatarUrl = msg.getIncomingAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Picasso.get().load(avatarUrl).centerCrop().fit().into(holder.ivIncomingAvatar);
            } else {
                Picasso.get()
                        .load("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400")
                        .centerCrop()
                        .fit()
                        .into(holder.ivIncomingAvatar);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llOutgoing, llIncoming;
        TextView tvOutgoingText, tvIncomingText;
        FrameLayout flOutgoingImage;
        ImageView ivOutgoingImage, ivIncomingAvatar;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            llOutgoing = itemView.findViewById(R.id.llOutgoing);
            llIncoming = itemView.findViewById(R.id.llIncoming);
            tvOutgoingText = itemView.findViewById(R.id.tvOutgoingText);
            tvIncomingText = itemView.findViewById(R.id.tvIncomingText);
            flOutgoingImage = itemView.findViewById(R.id.flOutgoingImage);
            ivOutgoingImage = itemView.findViewById(R.id.ivOutgoingImage);
            ivIncomingAvatar = itemView.findViewById(R.id.ivIncomingAvatar);
        }
    }
}
