package com.compass.repository.impl;

import com.compass.db.DatabaseManager;
import com.compass.model.CampusLocation;
import com.compass.repository.LocationRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcLocationRepository implements LocationRepository {

    @Override
    public CampusLocation save(CampusLocation entity) {
        String sql = "INSERT INTO campus_locations (location_name, latitude, longitude, building_code, description) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getLocationName());
            ps.setDouble(2, entity.getLatitude());
            ps.setDouble(3, entity.getLongitude());
            ps.setString(4, entity.getBuildingCode());
            ps.setString(5, entity.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setLocationId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CampusLocation update(CampusLocation entity) {
        String sql = "UPDATE campus_locations SET location_name=?, latitude=?, longitude=?, building_code=?, description=? WHERE location_id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getLocationName());
            ps.setDouble(2, entity.getLatitude());
            ps.setDouble(3, entity.getLongitude());
            ps.setString(4, entity.getBuildingCode());
            ps.setString(5, entity.getDescription());
            ps.setInt(6, entity.getLocationId());
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM campus_locations WHERE location_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CampusLocation> findById(Integer id) {
        List<CampusLocation> list = query("SELECT * FROM campus_locations WHERE location_id=?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<CampusLocation> findAll() {
        return query("SELECT * FROM campus_locations ORDER BY location_name");
    }

    @Override
    public boolean existsById(Integer id) {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM campus_locations WHERE location_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long count() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM campus_locations")) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CampusLocation> findByLocationName(String locationName) {
        List<CampusLocation> list = query("SELECT * FROM campus_locations WHERE location_name=?", locationName);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<CampusLocation> findByBuildingCode(String buildingCode) {
        List<CampusLocation> list = query("SELECT * FROM campus_locations WHERE building_code=?", buildingCode);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<CampusLocation> getAllLocationsForMap() {
        return findAll();
    }

    public List<int[]> getEdges() {
        List<int[]> edges = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT from_location_id, to_location_id, distance_meters FROM campus_edges")) {
            while (rs.next()) {
                edges.add(new int[]{
                        rs.getInt("from_location_id"),
                        rs.getInt("to_location_id"),
                        (int) rs.getDouble("distance_meters")
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return edges;
    }

    private List<CampusLocation> query(String sql, Object... params) {
        List<CampusLocation> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CampusLocation loc = new CampusLocation();
                    loc.setLocationId(rs.getInt("location_id"));
                    loc.setLocationName(rs.getString("location_name"));
                    loc.setLatitude(rs.getDouble("latitude"));
                    loc.setLongitude(rs.getDouble("longitude"));
                    loc.setBuildingCode(rs.getString("building_code"));
                    loc.setDescription(rs.getString("description"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        loc.setCreatedAt(created.toLocalDateTime());
                    }
                    list.add(loc);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
