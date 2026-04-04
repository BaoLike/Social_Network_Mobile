package com.example.social_network.Fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Activity.AddImageActivity;
import com.example.social_network.Activity.PostDetailActivity;
import com.example.social_network.Adapter.PostsGridAdapter;
import com.example.social_network.Data.PostRepository;
import com.example.social_network.Model.PostGridModel;
import com.example.social_network.R;

import java.util.List;

/**
 * Fragment that shows all of the current user's posts in a 3-column Instagram-
 * style grid.  Tapping a cell opens PostDetailActivity; the header "+" button
 * opens the full AddImageActivity → NewPostActivity create-flow.
 * The grid refreshes automatically when a new post is added via PostRepository.
 */
public class AddFragment extends Fragment implements PostRepository.OnPostsChangedListener {

    private static final int GRID_COLUMNS = 3;

    private RecyclerView     rvGrid;
    private LinearLayout     layoutEmpty;
    private PostsGridAdapter adapter;

    public static AddFragment newInstance() {
        return new AddFragment();
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add, container, false);

        rvGrid      = view.findViewById(R.id.rvPostsGrid);
        layoutEmpty = view.findViewById(R.id.layoutEmptyState);

        // Header compose button → open image picker
        view.findViewById(R.id.btnCreatePost)
                .setOnClickListener(v -> openAddImageFlow());

        // Empty-state button
        view.findViewById(R.id.btnFirstPost)
                .setOnClickListener(v -> openAddImageFlow());

        setupGrid();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the grid (handles new posts added while user was in create flow)
        refreshGrid();
    }

    @Override
    public void onStart() {
        super.onStart();
        PostRepository.getInstance().addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PostRepository.getInstance().removeListener(this);
    }

    // ── PostRepository listener callback ────────────────────────────────────
    @Override
    public void onPostAdded(PostGridModel post) {
        // No-op here – we refresh in onResume instead to be safe on UI thread
    }

    // ── Grid setup ──────────────────────────────────────────────────────────
    private void setupGrid() {
        GridLayoutManager glm = new GridLayoutManager(getContext(), GRID_COLUMNS);
        rvGrid.setLayoutManager(glm);

        // 1dp gap between cells
        int gapPx = (int) (1 * requireContext().getResources().getDisplayMetrics().density);
        rvGrid.addItemDecoration(new GridSpacingDecoration(GRID_COLUMNS, gapPx));

        refreshGrid();
    }

    private void refreshGrid() {
        List<PostGridModel> posts = PostRepository.getInstance().getPosts();

        if (posts.isEmpty()) {
            rvGrid.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        rvGrid.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        adapter = new PostsGridAdapter(posts, (post, position) -> {
            Intent intent = new Intent(getContext(), PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.KEY_POSITION, position);
            startActivity(intent);
        });

        rvGrid.setAdapter(adapter);
    }

    // ── Navigation ──────────────────────────────────────────────────────────
    private void openAddImageFlow() {
        startActivity(new Intent(getContext(), AddImageActivity.class));
    }

    // ── Inner class: grid spacing decoration ────────────────────────────────
    private static class GridSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;

        GridSpacingDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing   = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect out, @NonNull View view,
                                   @NonNull RecyclerView parent,
                                   @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int col      = position % spanCount;
            out.left   = col * spacing / spanCount;
            out.right  = spacing - (col + 1) * spacing / spanCount;
            out.bottom = spacing;
        }
    }
}
