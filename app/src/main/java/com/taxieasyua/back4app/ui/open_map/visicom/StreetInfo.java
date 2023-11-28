package com.taxieasyua.back4app.ui.open_map.visicom;

public class StreetInfo {
    private String streetType;
    private String street;
    private String name;
    private String zone;
    private String settlementType;
    private String settlement;
    private double longitude;
    private double latitude;

    public StreetInfo(String streetType, String street, String name, String zone, String settlementType, String settlement, double longitude, double latitude) {
        this.streetType = streetType;
        this.street = street;
        this.name = name;
        this.zone = zone;
        this.settlementType = settlementType;
        this.settlement = settlement;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Геттеры и сеттеры

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
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

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    public String getSettlement() {
        return settlement;
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
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
