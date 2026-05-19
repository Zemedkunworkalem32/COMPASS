package com.compass.model;

import java.util.Objects;

public class CampusLocation {
    private int id;
    private String name;
    private String building;
    private String floor;

    public CampusLocation() {
    }

    public CampusLocation(int id, String name, String building, String floor) {
        this.id = id;
        this.name = name;
        this.building = building;
        this.floor = floor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CampusLocation)) return false;
        CampusLocation that = (CampusLocation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
