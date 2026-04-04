package com.example.social_network.Adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Model.Story;
import com.example.social_network.R;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final List<Story> stories;
    private OnStoryClickListener listener;

    public interface OnStoryClickListener {
        void onStoryClick(Story story, int position);
    }

    public StoryAdapter(List<Story> stories) {
        this.stories = stories;
    }

    public void setOnStoryClickListener(OnStoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = stories.get(position);
        holder.bind(story);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStoryClick(story, position);
        });
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivStoryRing;
        private final ImageView ivAvatar;
        private final TextView tvAddStory;
        private final TextView tvLive;
        private final TextView tvUsername;

        StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStoryRing  = itemView.findViewById(R.id.ivStoryRing);
            ivAvatar     = itemView.findViewById(R.id.ivAvatar);
            tvAddStory   = itemView.findViewById(R.id.tvAddStory);
            tvLive       = itemView.findViewById(R.id.tvLive);
            tvUsername   = itemView.findViewById(R.id.tvUsername);
        }

        void bind(Story story) {
            // Avatar placeholder color
            GradientDrawable avatarBg = new GradientDrawable();
            avatarBg.setShape(GradientDrawable.OVAL);
            avatarBg.setColor(story.getUser().getAvatarColor());
            ivAvatar.setBackground(avatarBg);

            // Story ring: gradient if has new story or live, gray otherwise
            if (story.isOwn()) {
                ivStoryRing.setImageResource(R.drawable.story_ring_gray);
            } else if (story.hasNewStory() || story.isLive()) {
                ivStoryRing.setImageResource(R.drawable.story_ring_gradient);
            } else {
                ivStoryRing.setImageResource(R.drawable.story_ring_gray);
            }

            // "+" button for Your Story
            tvAddStory.setVisibility(story.isOwn() ? View.VISIBLE : View.GONE);

            // LIVE badge
            tvLive.setVisibility(story.isLive() ? View.VISIBLE : View.GONE);

            // Username
            tvUsername.setText(story.getUser().getUsername());
        }
    }
}
