package com.compass.repository;

import com.compass.model.Department;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DepartmentRepository implements Repository<Department> {
    @Override
    public Optional<Department> findById(int id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<Department> findAll() throws SQLException {
        return Collections.emptyList();
    }

    @Override
    public Department save(Department entity) throws SQLException {
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
    }
}
