package com.sns.wanderwise.checklist;

public class DestinationItem {

    private String name;
    private String travelType;
    private String travelMode;

    public DestinationItem(String name, String travelType, String travelMode) {

        this.name = name;
        this.travelType = travelType;
        this.travelMode = travelMode;
    }

    public String getName() {
        return name;
    }

    public String getTravelType() {
        return travelType;
    }

    public String getTravelMode() {
        return travelMode;
    }
}
