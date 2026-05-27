package com.compass.service;

import com.compass.repository.ComplaintRepository;
import com.compass.repository.DepartmentRepository;
import com.compass.repository.LocationRepository;
import com.compass.repository.UserRepository;
import com.compass.repository.impl.JdbcComplaintRepository;
import com.compass.repository.impl.JdbcDepartmentRepository;
import com.compass.repository.impl.JdbcLocationRepository;
import com.compass.repository.impl.JdbcUserRepository;

/**
 * Provides shared repository instances.
 */
public final class RepositoryFactory {
    private static final UserRepository USER_REPOSITORY = new JdbcUserRepository();
    private static final ComplaintRepository COMPLAINT_REPOSITORY = new JdbcComplaintRepository();
    private static final DepartmentRepository DEPARTMENT_REPOSITORY = new JdbcDepartmentRepository();
    private static final JdbcLocationRepository LOCATION_REPOSITORY = new JdbcLocationRepository();

    private RepositoryFactory() {}

    public static UserRepository users() { return USER_REPOSITORY; }
    public static ComplaintRepository complaints() { return COMPLAINT_REPOSITORY; }
    public static DepartmentRepository departments() { return DEPARTMENT_REPOSITORY; }
    public static JdbcLocationRepository locations() { return LOCATION_REPOSITORY; }
}
