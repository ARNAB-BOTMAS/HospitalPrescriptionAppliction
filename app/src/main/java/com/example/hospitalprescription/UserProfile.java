package com.example.hospitalprescription;

public class UserProfile {
    private String displayName;
    private String email;
    private String password;

    // Empty constructor needed for Firebase
    public UserProfile() {}

    public UserProfile(String displayName, String email, String password) {
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword(){
        return password;
    }
}

