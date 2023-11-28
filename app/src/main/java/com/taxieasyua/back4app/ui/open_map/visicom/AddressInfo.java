package com.taxieasyua.back4app.ui.open_map.visicom;

public class AddressInfo {
    private String type;
    private String name;
    private String zone;
    private double longitude;
    private double latitude;

    public AddressInfo(String type, String name, String zone, double longitude, double latitude) {
        this.type = type;
        this.name = name;
        this.zone = zone;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Геттеры и сеттеры

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}

