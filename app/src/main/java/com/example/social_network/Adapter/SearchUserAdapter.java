package com.example.social_network.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Model.SearchUserResult;
import com.example.social_network.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.VH> {

    public interface OnUserClickListener {
        void onUserClick(SearchUserResult user);
    }

    private final List<SearchUserResult> items = new ArrayList<>();
    private final OnUserClickListener listener;

    public SearchUserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SearchUserResult> users) {
        items.clear();
        if (users != null) {
            items.addAll(users);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SearchUserResult u = items.get(position);
        holder.tvName.setText(u.getDisplayName());
        String sub = u.getFullName();
        if (sub.isEmpty()) {
            sub = u.getUserId();
        }
        holder.tvSubtitle.setText(sub);

        String av = u.getAvatar();
        Picasso.get().cancelRequest(holder.ivAvatar);
        if (av != null && !av.isEmpty()) {
            Picasso.get().load(av).centerCrop().fit().into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(v -> listener.onUserClick(u));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivAvatar;
        final TextView tvName;
        final TextView tvSubtitle;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivSearchUserAvatar);
            tvName = itemView.findViewById(R.id.tvSearchUserName);
            tvSubtitle = itemView.findViewById(R.id.tvSearchUserSubtitle);
        }
    }
}
