package com.example.social_network.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Adapter.FeedAdapter;
import com.example.social_network.Data.DummyData;
import com.example.social_network.Model.Post;
import com.example.social_network.R;
import com.example.social_network.Utils.FetchApi;
import com.example.social_network.Utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final long BOTTOM_REFRESH_COOLDOWN_MS = 2000;
    private FeedAdapter feedAdapter;
    private boolean isFetchingPosts = false;
    private long lastBottomRefreshAt = 0L;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFeed(view);
        setupTopBar(view);
    }

    private void setupFeed(View view) {
        feedAdapter = new FeedAdapter(
                DummyData.getStories(),
                DummyData.getPosts()
        );
        RecyclerView rvFeed = view.findViewById(R.id.rvFeed);
        rvFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy >= 0) return;
                if (!recyclerView.canScrollVertically(-1)) {
                    triggerTopRefresh();
                }
            }
        });
        fetchPostsFromServer();
    }

    private void fetchPostsFromServer() {
        if (isFetchingPosts) return;
        isFetchingPosts = true;
        if (!isAdded()) {
            isFetchingPosts = false;
            return;
        }
        if (TokenManager.getAccessToken(requireContext()) == null) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            isFetchingPosts = false;
            return;
        }

        new FetchApi().getPosts(requireContext(), TAG, new FetchApi.PostsCallback() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (!isAdded()) {
                    isFetchingPosts = false;
                    return;
                }
                List<Post> safePosts = posts != null ? posts : new ArrayList<>();
                feedAdapter = new FeedAdapter(DummyData.getStories(), safePosts);
                View currentView = getView();
                if (currentView == null) {
                    isFetchingPosts = false;
                    return;
                }
                RecyclerView rvFeed = currentView.findViewById(R.id.rvFeed);
                rvFeed.setAdapter(feedAdapter);
                isFetchingPosts = false;
            }

            @Override
            public void onFailure(String message) {
                if (!isAdded()) {
                    isFetchingPosts = false;
                    return;
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                isFetchingPosts = false;
            }
        });
    }

    private void triggerTopRefresh() {
        long now = System.currentTimeMillis();
        if (now - lastBottomRefreshAt < BOTTOM_REFRESH_COOLDOWN_MS) {
            return;
        }
        lastBottomRefreshAt = now;
        fetchPostsFromServer();
    }

    private void setupTopBar(View view) {
        view.findViewById(R.id.btnCamera).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Camera", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btnDm).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Direct Messages", Toast.LENGTH_SHORT).show());
    }
}
