package com.example.social_network.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.Activity.OtherUserProfileActivity;
import com.example.social_network.Adapter.SearchUserAdapter;
import com.example.social_network.Model.SearchUserResult;
import com.example.social_network.Network.ProfileApiService;
import com.example.social_network.R;
import com.example.social_network.Utils.TokenManager;

import java.util.List;

public class SearchFragment extends Fragment {

    private static final int SEARCH_DEBOUNCE_MS = 400;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private final ProfileApiService profileApi = new ProfileApiService();

    private EditText etKeyword;
    private RecyclerView rvResults;
    private TextView tvEmpty;
    private SearchUserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etKeyword = view.findViewById(R.id.etSearchKeyword);
        rvResults = view.findViewById(R.id.rvSearchUsers);
        tvEmpty = view.findViewById(R.id.tvSearchEmpty);

        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SearchUserAdapter(this::openUserProfile);
        rvResults.setAdapter(adapter);

        etKeyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                runSearchNow();
                return true;
            }
            return false;
        });

        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (debounceRunnable != null) {
                    debounceHandler.removeCallbacks(debounceRunnable);
                }
                debounceRunnable = () -> runSearch(s.toString());
                debounceHandler.postDelayed(debounceRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
    }

    private void runSearchNow() {
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
        runSearch(etKeyword != null ? etKeyword.getText().toString() : "");
    }

    private void runSearch(String raw) {
        if (etKeyword == null || !isAdded()) {
            return;
        }
        String q = raw.trim();
        if (q.isEmpty()) {
            adapter.setItems(null);
            updateEmptyState(true, "Gõ tên đăng nhập để tìm người dùng");
            return;
        }

        if (!TokenManager.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        profileApi.searchUsers(requireContext(), q, new ProfileApiService.SearchUsersCallback() {
            @Override
            public void onSuccess(List<SearchUserResult> users) {
                if (!isAdded() || adapter == null) {
                    return;
                }
                adapter.setItems(users);
                updateEmptyState(users.isEmpty(), users.isEmpty() ? "Không tìm thấy người dùng" : null);
            }

            @Override
            public void onFailure(String message) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEmptyState(boolean showEmpty, String message) {
        if (tvEmpty == null) {
            return;
        }
        tvEmpty.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
        if (message != null) {
            tvEmpty.setText(message);
        }
    }

    private void openUserProfile(SearchUserResult user) {
        Intent intent = new Intent(requireContext(), OtherUserProfileActivity.class);
        intent.putExtra(OtherUserProfileActivity.EXTRA_USER, user);
        startActivity(intent);
    }
}
