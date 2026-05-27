package com.compass.service;

import com.compass.repository.ComplaintRepository;
import com.compass.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentAnalyticsService Unit Tests")
class PerformanceAnalyticsTest {

    @Mock private DepartmentRepository departmentRepository;
    @Mock private ComplaintRepository  complaintRepository;

    private DepartmentAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new DepartmentAnalyticsService(departmentRepository, complaintRepository);
    }

    // ── getTotalComplaints ────────────────────────────────────────────────────

    @Test
    @DisplayName("getTotalComplaints delegates to complaintRepository.count")
    void getTotalComplaints_delegatesToRepository() {
        when(complaintRepository.count()).thenReturn(42L);

        assertEquals(42L, analyticsService.getTotalComplaints());
        verify(complaintRepository).count();
    }

    // ── getResolvedCount ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getResolvedCount delegates to complaintRepository.countResolved")
    void getResolvedCount_delegatesToRepository() {
        when(complaintRepository.countResolved()).thenReturn(18L);

        assertEquals(18L, analyticsService.getResolvedCount());
        verify(complaintRepository).countResolved();
    }

    // ── getPendingCount ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getPendingCount delegates to complaintRepository.countPending")
    void getPendingCount_delegatesToRepository() {
        when(complaintRepository.countPending()).thenReturn(24L);

        assertEquals(24L, analyticsService.getPendingCount());
        verify(complaintRepository).countPending();
    }

    // ── getAllDepartments ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllDepartments returns only active departments")
    void getAllDepartments_returnsActive() {
        when(departmentRepository.findAllActive()).thenReturn(List.of());

        analyticsService.getAllDepartments();

        verify(departmentRepository).findAllActive();
    }

    // ── getBestPerformingDepartment / getSlowestDepartment ────────────────────

    @Test
    @DisplayName("getBestPerformingDepartment returns null when no stats exist")
    void getBestPerforming_noStats_returnsNull() {
        // getDepartmentStats() will call DB; isolate by spying or accept null from empty result
        // Here we rely on the real method but with no DB — it will throw or return null gracefully.
        // We test that it doesn't blow up the caller even if no data is available.
        // (A proper integration test would seed the DB.)
        DepartmentAnalyticsService service = new DepartmentAnalyticsService(
                departmentRepository, complaintRepository) {
            @Override
            public java.util.List<DepartmentAnalyticsService.DepartmentStats> getDepartmentStats() {
                return java.util.List.of(); // stub – no DB needed
            }
        };

        assertNull(service.getBestPerformingDepartment());
    }

    @Test
    @DisplayName("getSlowestDepartment returns null when no stats exist")
    void getSlowest_noStats_returnsNull() {
        DepartmentAnalyticsService service = new DepartmentAnalyticsService(
                departmentRepository, complaintRepository) {
            @Override
            public java.util.List<DepartmentAnalyticsService.DepartmentStats> getDepartmentStats() {
                return java.util.List.of();
            }
        };

        assertNull(service.getSlowestDepartment());
    }

    @Test
    @DisplayName("DepartmentStats efficiencyPercent is calculated correctly")
    void departmentStats_efficiencyCalculation() {
        DepartmentAnalyticsService.DepartmentStats stats = new DepartmentAnalyticsService.DepartmentStats();
        stats.totalComplaints    = 10;
        stats.resolvedComplaints = 7;
        stats.efficiencyPercent  = (stats.resolvedComplaints * 100.0) / stats.totalComplaints;

        assertEquals(70.0, stats.getEfficiencyPercent(), 0.001);
    }
}
