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
import com.example.social_network.R;

public class HomeFragment extends Fragment {

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
        FeedAdapter adapter = new FeedAdapter(
                DummyData.getStories(),
                DummyData.getPosts()
        );
        RecyclerView rvFeed = view.findViewById(R.id.rvFeed);
        rvFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFeed.setAdapter(adapter);
    }

    private void setupTopBar(View view) {
        view.findViewById(R.id.btnCamera).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Camera", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btnDm).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Direct Messages", Toast.LENGTH_SHORT).show());
    }
}
