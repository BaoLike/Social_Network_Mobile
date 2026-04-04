package com.example.social_network.Adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Model.ConversationModel;
import com.example.social_network.R;

import java.util.List;

/**
 * RecyclerView adapter for the Direct Messages conversation list.
 * Each row shows a coloured avatar circle (first letter of username),
 * the username, last message preview, and the time since last message.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(ConversationModel conversationModel);
    }

    private final List<ConversationModel> conversations;
    private final OnConversationClickListener onConversationClickListener;

    public MessagesAdapter(List<ConversationModel> conversations,
                           OnConversationClickListener onConversationClickListener) {
        this.conversations = conversations;
        this.onConversationClickListener = onConversationClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationModel conv = conversations.get(position);

        // --- Avatar: coloured oval with the first character of the username ---
        String initial = conv.getUsername().isEmpty()
                ? "?"
                : String.valueOf(conv.getUsername().charAt(0)).toUpperCase();
        holder.tvAvatarLetter.setText(initial);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(conv.getAvatarColor());
        holder.tvAvatarLetter.setBackground(circle);

        // --- Text fields ---
        holder.tvUsername.setText(conv.getUsername());
        holder.tvLastMessage.setText(conv.getLastMessage());
        holder.tvTime.setText(conv.getTimeAgo());

        // --- Click to open Chat Detail ---
        holder.itemView.setOnClickListener(v -> onConversationClickListener.onConversationClick(conv));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    // -------------------------------------------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAvatarLetter;
        final TextView tvUsername;
        final TextView tvLastMessage;
        final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter = itemView.findViewById(R.id.tvAvatarLetter);
            tvUsername     = itemView.findViewById(R.id.tvConvUsername);
            tvLastMessage  = itemView.findViewById(R.id.tvLastMessage);
            tvTime         = itemView.findViewById(R.id.tvTime);
        }
    }
}
