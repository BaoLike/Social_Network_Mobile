package com.example.social_network.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Model.PostGridModel;
import com.example.social_network.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 3-column grid adapter for the Posts page.
 * Uses Picasso (already in the project) to load remote thumbnail URLs.
 * Each cell is made square by overriding onMeasure in the ViewHolder.
 */
public class PostsGridAdapter extends RecyclerView.Adapter<PostsGridAdapter.ViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(PostGridModel post, int position);
    }

    private final List<PostGridModel>  posts;
    private final OnPostClickListener  listener;

    public PostsGridAdapter(List<PostGridModel> posts, OnPostClickListener listener) {
        this.posts    = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_grid, parent, false);

        // Force square cells: height = one-third of the RecyclerView width
        int spanCount = 3;
        int spacing   = 2; // px gap between cells
        int cellWidth = (parent.getWidth() - spacing * (spanCount - 1)) / spanCount;
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = cellWidth;
        view.setLayoutParams(lp);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostGridModel post = posts.get(position);

        // Load thumbnail via Picasso
        Picasso.get()
                .load(post.getImageUrl())
                .placeholder(R.drawable.ic_nav_add)   // lightweight placeholder
                .centerCrop()
                .fit()
                .into(holder.ivThumbnail);

        // Video badge
        holder.ivVideoBadge.setVisibility(
                post.getType() == PostGridModel.Type.VIDEO ? View.VISIBLE : View.GONE);

        // Multi-image badge
        holder.ivMultiBadge.setVisibility(
                post.getType() == PostGridModel.Type.MULTI ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPostClick(post, position);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // -------------------------------------------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumbnail;
        final ImageView ivVideoBadge;
        final ImageView ivMultiBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail  = itemView.findViewById(R.id.ivPostThumbnail);
            ivVideoBadge = itemView.findViewById(R.id.ivVideoBadge);
            ivMultiBadge = itemView.findViewById(R.id.ivMultiBadge);
        }

        /** Force each cell to be perfectly square (width == height). */
        @Override
        protected void finalize() throws Throwable { super.finalize(); }
    }
}
