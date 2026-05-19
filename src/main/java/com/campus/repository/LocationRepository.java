package com.campus.repository;

import com.campus.model.CampusLocation;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LocationRepository implements Repository<CampusLocation> {
    @Override
    public Optional<CampusLocation> findById(int id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<CampusLocation> findAll() throws SQLException {
        return Collections.emptyList();
    }

    @Override
    public CampusLocation save(CampusLocation entity) throws SQLException {
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
    }
}

