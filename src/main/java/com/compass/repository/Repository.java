package com.compass.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    Optional<T> findById(int id) throws SQLException;
    List<T> findAll() throws SQLException;
    T save(T entity) throws SQLException;
    void delete(int id) throws SQLException;
}
