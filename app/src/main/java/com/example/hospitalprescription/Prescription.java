package com.example.hospitalprescription;

public class Prescription {

    private String date;
    private String imageUrl;

    public Prescription() {}

    public Prescription(String date, String imageUrl) {
        this.date = date;
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
