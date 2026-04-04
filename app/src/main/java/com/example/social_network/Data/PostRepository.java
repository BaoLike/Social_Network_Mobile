package com.example.social_network.Data;

import com.example.social_network.Model.PostGridModel;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory singleton that holds the user's post list.
 * All screens share the same instance, so adding a post from AddImageActivity
 * is immediately visible in AddFragment without a database.
 */
public class PostRepository {

    private static PostRepository instance;
    private final List<PostGridModel>         posts    = new ArrayList<>();
    private final List<OnPostsChangedListener> listeners = new ArrayList<>();

    public interface OnPostsChangedListener {
        void onPostAdded(PostGridModel post);
    }

    private PostRepository() {
        // Seed with 9 mock posts
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600",
                PostGridModel.Type.IMAGE, 234,
                "jacob_w", "Enjoying the outdoors 🌿", "2h"));
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600",
                PostGridModel.Type.VIDEO, 512,
                "jacob_w", "Mountain vibes 🏔️", "5h"));
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1503023345310-bd7428a21914?w=600",
                PostGridModel.Type.IMAGE, 89,
                "jacob_w", "Portrait mode on 📸", "1d"));
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600",
                PostGridModel.Type.MULTI, 310,
                "jacob_w", "Colourful collection 🎨", "2d"));
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1490750967868-88df5691cc9d?w=600",
                PostGridModel.Type.IMAGE, 670,
                "jacob_w", "Spring flowers 🌸", "3d"));
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=600",
                PostGridModel.Type.VIDEO, 145,
                "jacob_w", "Coffee and work ☕", "4d"));
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1545231027-637d2f6210f8?w=600",
                PostGridModel.Type.IMAGE, 421,
                "jacob_w", "City lights at night 🌃", "5d"));
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1527090526205-beaac8dc3c62?w=600",
                PostGridModel.Type.MULTI, 198,
                "jacob_w", "Weekend memories 📖", "1w"));
        posts.add(new PostGridModel(
                "https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?w=600",
                PostGridModel.Type.IMAGE, 876,
                "jacob_w", "Urban exploration 🏙️", "1w"));
    }

    public static synchronized PostRepository getInstance() {
        if (instance == null) instance = new PostRepository();
        return instance;
    }

    public List<PostGridModel> getPosts() {
        return posts;
    }

    /** Prepends a new post and notifies all registered listeners. */
    public void addPost(PostGridModel post) {
        posts.add(0, post);
        for (OnPostsChangedListener l : listeners) l.onPostAdded(post);
    }

    public void addListener(OnPostsChangedListener l)    { listeners.add(l); }
    public void removeListener(OnPostsChangedListener l) { listeners.remove(l); }
}
