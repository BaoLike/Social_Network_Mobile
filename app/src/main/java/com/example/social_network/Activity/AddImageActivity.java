package com.example.social_network.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_network.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Instagram-style image picker:
 *   – Top bar: Cancel | Recents ▾ | Next
 *   – Large preview of the selected photo
 *   – Scrollable 4-column gallery grid
 *   – Bottom tab bar: Library | Photo | Video
 *
 * On "Next" → launches NewPostActivity with the selected image URL.
 */
public class AddImageActivity extends AppCompatActivity {

    public static final String KEY_IMAGE_URL = "selected_image_url";

    // Unsplash sample photos representing "Recents" (fallback if no permission or no images)
    private static final List<String> FALLBACK_GALLERY_URLS = Arrays.asList(
            "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
            "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400",
            "https://images.unsplash.com/photo-1503023345310-bd7428a21914?w=400",
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400",
            "https://images.unsplash.com/photo-1490750967868-88df5691cc9d?w=400",
            "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=400",
            "https://images.unsplash.com/photo-1545231027-637d2f6210f8?w=400",
            "https://images.unsplash.com/photo-1527090526205-beaac8dc3c62?w=400",
            "https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?w=400",
            "https://images.unsplash.com/photo-1519125323398-675f0ddb6308?w=400",
            "https://images.unsplash.com/photo-1463746163368-405fd625c0e0?w=400",
            "https://images.unsplash.com/photo-1454496522488-7a8e488e8606?w=400"
    );

    private String selectedUrl;
    private ImageView ivPreview;
    private GalleryAdapter galleryAdapter;
    private final List<String> deviceGalleryUrls = new ArrayList<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadImagesFromDevice();
                } else {
                    Toast.makeText(this, "Quyền truy cập bị từ chối. Hiện ảnh mẫu.", Toast.LENGTH_SHORT).show();
                    loadMockImages();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        ivPreview = findViewById(R.id.ivPreview);

        setupGallery();
        setupButtons();
        checkPermissionsAndLoadImages();
    }

    private void checkPermissionsAndLoadImages() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadImagesFromDevice();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void loadImagesFromDevice() {
        deviceGalleryUrls.clear();
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(dataColumn);
                    deviceGalleryUrls.add("file://" + imagePath); // Picasso loves file:// for local paths
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (deviceGalleryUrls.isEmpty()) {
            loadMockImages();
        } else {
            selectedUrl = deviceGalleryUrls.get(0);
            loadPreview(selectedUrl);
            galleryAdapter.setUrls(deviceGalleryUrls);
            galleryAdapter.setSelected(selectedUrl);
        }
    }

    private void loadMockImages() {
        deviceGalleryUrls.clear();
        deviceGalleryUrls.addAll(FALLBACK_GALLERY_URLS);
        if (!deviceGalleryUrls.isEmpty()) {
            selectedUrl = deviceGalleryUrls.get(0);
            loadPreview(selectedUrl);
        }
        galleryAdapter.setUrls(deviceGalleryUrls);
        galleryAdapter.setSelected(selectedUrl);
    }

    // ── Load the large preview ──────────────────────────────────────────────
    private void loadPreview(String url) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url).centerCrop().fit().into(ivPreview);
        }
    }

    // ── 4-column gallery grid ───────────────────────────────────────────────
    private void setupGallery() {
        RecyclerView rv = findViewById(R.id.rvGallery);
        rv.setLayoutManager(new GridLayoutManager(this, 4));
        galleryAdapter = new GalleryAdapter(deviceGalleryUrls, selectedUrl, url -> {
            selectedUrl = url;
            loadPreview(selectedUrl);
            galleryAdapter.setSelected(selectedUrl);
        });
        rv.setAdapter(galleryAdapter);
    }

    // ── Cancel / Next / Tabs ────────────────────────────────────────────────
    private void setupButtons() {
        // Cancel → finish
        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());

        // Next → open NewPostActivity with selected URL
        findViewById(R.id.tvNext).setOnClickListener(v -> {
            if (selectedUrl != null && !selectedUrl.isEmpty()) {
                Intent intent = new Intent(this, NewPostActivity.class);
                intent.putExtra(KEY_IMAGE_URL, selectedUrl);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        });

        // Tab bar highlight switching (visual only – no real camera/video)
        TextView tabLibrary = findViewById(R.id.tabLibrary);
        TextView tabPhoto   = findViewById(R.id.tabPhoto);
        TextView tabVideo   = findViewById(R.id.tabVideo);

        tabLibrary.setOnClickListener(v -> activateTab(tabLibrary, tabPhoto, tabVideo));
        tabPhoto  .setOnClickListener(v -> activateTab(tabPhoto,   tabLibrary, tabVideo));
        tabVideo  .setOnClickListener(v -> activateTab(tabVideo,   tabLibrary, tabPhoto));
    }

    private void activateTab(TextView active, TextView... others) {
        active.setTextColor(Color.parseColor("#000000"));
        active.setTextSize(13);
        active.setTypeface(null, android.graphics.Typeface.BOLD);
        for (TextView t : others) {
            t.setTextColor(Color.parseColor("#8E8E8E"));
            t.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    // ── Inner Adapter ───────────────────────────────────────────────────────
    private static class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VH> {

        interface OnSelectListener { void onSelect(String url); }

        private List<String>   urls;
        private String         selectedUrl;
        private final OnSelectListener listener;

        GalleryAdapter(List<String> urls, String selectedUrl, OnSelectListener l) {
            this.urls        = new ArrayList<>(urls);
            this.selectedUrl = selectedUrl;
            this.listener    = l;
        }

        void setUrls(List<String> urls) {
            this.urls = new ArrayList<>(urls);
            notifyDataSetChanged();
        }

        void setSelected(String url) {
            selectedUrl = url;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_post_grid, parent, false);
            // Square cells
            int cellW = parent.getWidth() / 4;
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height = cellW;
            view.setLayoutParams(lp);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            String url = urls.get(position);
            Picasso.get().load(url).centerCrop().fit().into(h.iv);
            // Blue overlay for selected cell
            h.overlay.setVisibility(url.equals(selectedUrl) ? View.VISIBLE : View.GONE);
            h.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSelect(url);
            });
        }

        @Override public int getItemCount() { return urls.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final ImageView iv;
            final View      overlay;

            VH(@NonNull View v) {
                super(v);
                iv      = v.findViewById(R.id.ivPostThumbnail);
                overlay = v.findViewById(R.id.viewSelectionOverlay);
            }
        }
    }
}
