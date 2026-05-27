package com.compass.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CampusLocation Model - Represents a location on campus
 * TEAMMATE 1: Abdissa
 */
public class CampusLocation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int locationId;
    private String locationName;
    private double latitude;
    private double longitude;
    private String buildingCode;
    private String description;
    private LocalDateTime createdAt;

    // Constructors
    public CampusLocation() {}

    public CampusLocation(String locationName, double latitude, double longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getBuildingCode() { return buildingCode; }
    public void setBuildingCode(String buildingCode) { this.buildingCode = buildingCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "CampusLocation{" +
                "locationId=" + locationId +
                ", locationName='" + locationName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
