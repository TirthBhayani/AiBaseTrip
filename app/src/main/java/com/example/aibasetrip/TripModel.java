package com.example.aibasetrip;

public class TripModel {
    String title;
    String details;
    String hotel;

    public TripModel(String title, String details, String hotel) {
        this.title = title;
        this.details = details;
        this.hotel = hotel;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    public String getHotel() {
        return hotel;
    }
}
