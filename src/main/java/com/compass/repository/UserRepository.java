package com.compass.repository;

import com.compass.model.User;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserRepository implements Repository<User> {
    @Override
    public Optional<User> findById(int id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws SQLException {
        return Collections.emptyList();
    }

    @Override
    public User save(User entity) throws SQLException {
        return entity;
    }

    @Override
    public void delete(int id) throws SQLException {
    }
}
