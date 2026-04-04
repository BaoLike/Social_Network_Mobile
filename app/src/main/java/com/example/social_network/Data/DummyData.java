package com.example.social_network.Data;

import android.graphics.Color;

import com.example.social_network.Model.Post;
import com.example.social_network.Model.Story;
import com.example.social_network.Model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DummyData {

    public static List<Story> getStories() {
        List<Story> stories = new ArrayList<>();

        stories.add(new Story(
                new User("0", "Your Story", "", Color.parseColor("#E0E0E0"), false),
                true, false, false));
        stories.add(new Story(
                new User("1", "karennne", "", Color.parseColor("#9C27B0"), false),
                false, true, true));
        stories.add(new Story(
                new User("2", "zackjohn", "", Color.parseColor("#FF5722"), false),
                false, false, true));
        stories.add(new Story(
                new User("3", "kieron_d", "", Color.parseColor("#2196F3"), false),
                false, false, true));
        stories.add(new Story(
                new User("4", "craig_love", "", Color.parseColor("#4CAF50"), false),
                false, false, true));
        stories.add(new Story(
                new User("5", "maria_k", "", Color.parseColor("#FF9800"), false),
                false, false, false));
        stories.add(new Story(
                new User("6", "alex_ph", "", Color.parseColor("#E91E63"), false),
                false, false, true));
        stories.add(new Story(
                new User("7", "tom_snap", "", Color.parseColor("#00BCD4"), false),
                false, false, true));

        return stories;
    }

    public static List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();

        User joshua = new User("10", "joshua_l", "Tokyo, Japan", Color.parseColor("#3F51B5"), true);
        posts.add(new Post(
                joshua,
                Arrays.asList(Color.parseColor("#26C6DA"), Color.parseColor("#4CAF50"), Color.parseColor("#FF7043")),
                "craig_love", 44686,
                "The game in Japan was amazing and I want to share some photos",
                "2 hours ago", false, false));

        User sara = new User("11", "sara_travel", "Paris, France", Color.parseColor("#E91E63"), false);
        posts.add(new Post(
                sara,
                Arrays.asList(Color.parseColor("#78909C"), Color.parseColor("#A5D6A7")),
                "mike_photos", 12453,
                "La vie est belle ✨ Paris always takes my breath away! #paris #travel",
                "5 hours ago", true, true));

        User alex = new User("12", "alex_photo", "New York, USA", Color.parseColor("#FF5722"), false);
        posts.add(new Post(
                alex,
                Arrays.asList(Color.parseColor("#FFCA28")),
                "john_doe", 8923,
                "Golden hour in NYC 🌆 Nothing beats this view from the rooftop",
                "1 day ago", false, false));

        User mia = new User("13", "mia_style", "Milan, Italy", Color.parseColor("#9C27B0"), true);
        posts.add(new Post(
                mia,
                Arrays.asList(Color.parseColor("#EF9A9A"), Color.parseColor("#F48FB1")),
                "fashion_lover", 31200,
                "New collection drop 🔥 Which look is your favorite? Drop a comment below! #fashion #style",
                "2 days ago", true, false));

        User kevin = new User("14", "kevin_fit", "Los Angeles, USA", Color.parseColor("#FF9800"), false);
        posts.add(new Post(
                kevin,
                Arrays.asList(Color.parseColor("#81C784")),
                "fit_community", 5670,
                "Morning run done ✅ 10km personal best! Keep pushing your limits 💪 #fitness #running",
                "3 days ago", false, true));

        return posts;
    }
}
