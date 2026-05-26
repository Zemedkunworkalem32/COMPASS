package com.compass.repository;

import com.compass.model.CampusLocation;
import java.util.Optional;

/**
 * Campus Location Repository Interface
 * TEAMMATE 1: Abdissa
 */
public interface LocationRepository extends Repository<CampusLocation, Integer> {
    /**
     * Find location by name
     */
    Optional<CampusLocation> findByLocationName(String locationName);

    /**
     * Find location by building code
     */
    Optional<CampusLocation> findByBuildingCode(String buildingCode);

    /**
     * Get all locations for map display
     */
    java.util.List<CampusLocation> getAllLocationsForMap();
}
