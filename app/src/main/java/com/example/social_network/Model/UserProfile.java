package com.example.social_network.Model;

/**
 * Profile payload from GET /profile/info/getProfile and PUT update response.
 */
public class UserProfile {
    private final String userId;
    private final String userName;
    private final String avatar;
    private final String firstName;
    private final String lastName;
    private final String gender;
    private final String dob;
    private final String address;
    private final String phone;
    private final int follower;
    private final int followed;

    public UserProfile(String userId, String userName, String avatar,
                       String firstName, String lastName,
                       String gender, String dob, String address, String phone,
                       int follower, int followed) {
        this.userId = userId;
        this.userName = userName;
        this.avatar = avatar;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dob = dob;
        this.address = address;
        this.phone = phone;
        this.follower = follower;
        this.followed = followed;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getDob() {
        return dob;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public int getFollower() {
        return follower;
    }

    public int getFollowed() {
        return followed;
    }

    /** Display name: userName, else first + last. */
    public String getDisplayName() {
        if (userName != null && !userName.isEmpty()) {
            return userName;
        }
        String full = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        return full.isEmpty() ? (userId != null ? userId : "") : full;
    }
}
