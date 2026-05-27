package com.compass.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository Interface
 * TEAMMATE 1: Abdissa
 * 
 * Base interface for all repository implementations
 */
public interface Repository<T, ID> {
    /**
     * Save a new entity
     */
    T save(T entity);

    /**
     * Update an existing entity
     */
    T update(T entity);

    /**
     * Delete an entity by ID
     */
    boolean delete(ID id);

    /**
     * Find entity by ID
     */
    Optional<T> findById(ID id);

    /**
     * Find all entities
     */
    List<T> findAll();

    /**
     * Check if entity exists by ID
     */
    boolean existsById(ID id);

    /**
     * Get total count of entities
     */
    long count();
}
