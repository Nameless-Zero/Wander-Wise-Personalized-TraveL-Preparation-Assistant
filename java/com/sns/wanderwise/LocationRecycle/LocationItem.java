package com.sns.wanderwise.LocationRecycle;

public class LocationItem {
    private int id;
    private String location;
    private String image;
    private String description;
    private String transportMode;
    private String viewMore;

    public LocationItem(int id, String location, String image, String description, String transportMode, String viewMore) {
        this.id = id;
        this.location = location;
        this.image = image;
        this.description = description;
        this.transportMode = transportMode;
        this.viewMore = viewMore;
    }

    public int getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public String getViewMore() {
        return viewMore;
    }
}