package com.compass.repository;

import com.compass.model.Complaint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for the ComplaintRepository interface.
 * Uses an in-memory stub so no database is needed.
 */
@DisplayName("ComplaintRepository Contract Tests")
class ComplaintRepositoryTest {

    /** Minimal in-memory stub that fulfils the interface contract. */
    private static class InMemoryComplaintRepository implements ComplaintRepository {
        private final java.util.List<Complaint> store = new java.util.ArrayList<>();
        private int nextId = 1;

        @Override public Complaint save(Complaint e) {
            e.setComplaintId(nextId++);
            store.add(e);
            return e;
        }
        @Override public Complaint update(Complaint e) {
            store.replaceAll(c -> c.getComplaintId() == e.getComplaintId() ? e : c);
            return e;
        }
        @Override public boolean delete(Integer id) {
            return store.removeIf(c -> c.getComplaintId() == id);
        }
        @Override public Optional<Complaint> findById(Integer id) {
            return store.stream().filter(c -> c.getComplaintId() == id).findFirst();
        }
        @Override public List<Complaint> findAll() { return List.copyOf(store); }
        @Override public boolean existsById(Integer id) {
            return store.stream().anyMatch(c -> c.getComplaintId() == id);
        }
        @Override public long count() { return store.size(); }

        @Override public List<Complaint> findByStudentId(int studentId) {
            return store.stream().filter(c -> c.getStudentId() == studentId).toList();
        }
        @Override public List<Complaint> findByDepartmentId(int departmentId) {
            return store.stream()
                    .filter(c -> c.getAssignedDepartmentId() != null
                                 && c.getAssignedDepartmentId() == departmentId).toList();
        }
        @Override public List<Complaint> findByStatus(String status) {
            return store.stream().filter(c -> c.getStatus().name().equals(status)).toList();
        }
        @Override public List<Complaint> findByPriority(String priority) {
            return store.stream().filter(c -> c.getPriority().name().equals(priority)).toList();
        }
        @Override public List<Complaint> findByLocationId(int locationId) {
            return store.stream()
                    .filter(c -> c.getLocationId() != null && c.getLocationId() == locationId).toList();
        }
        @Override public List<Complaint> filterComplaints(Integer studentId, Integer departmentId,
                                                          String status, String priority) {
            return store.stream()
                    .filter(c -> studentId == null || c.getStudentId() == studentId)
                    .filter(c -> departmentId == null ||
                                 (c.getAssignedDepartmentId() != null && c.getAssignedDepartmentId().equals(departmentId)))
                    .filter(c -> status == null || "ALL".equals(status) || c.getStatus().name().equals(status))
                    .filter(c -> priority == null || "ALL".equals(priority) || c.getPriority().name().equals(priority))
                    .toList();
        }
        @Override public List<Complaint> getPendingComplaints(int departmentId) {
            return store.stream()
                    .filter(c -> c.getAssignedDepartmentId() != null && c.getAssignedDepartmentId() == departmentId)
                    .filter(c -> c.getStatus() != Complaint.ComplaintStatus.RESOLVED
                                 && c.getStatus() != Complaint.ComplaintStatus.CLOSED).toList();
        }
        @Override public long countResolved() {
            return store.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.RESOLVED).count();
        }
        @Override public long countPending() {
            return store.stream().filter(c -> c.getStatus() != Complaint.ComplaintStatus.RESOLVED
                                              && c.getStatus() != Complaint.ComplaintStatus.CLOSED).count();
        }
    }

    private final InMemoryComplaintRepository repo = new InMemoryComplaintRepository();

    private Complaint makeComplaint(int studentId, int deptId,
                                    Complaint.ComplaintStatus status,
                                    Complaint.ComplaintPriority priority) {
        Complaint c = new Complaint();
        c.setStudentId(studentId);
        c.setTitle("Test complaint");
        c.setDescription("Detailed description of the complaint.");
        c.setCategory("IT");
        c.setStatus(status);
        c.setPriority(priority);
        c.setAssignedDepartmentId(deptId);
        return c;
    }

    @Test @DisplayName("save assigns an ID and increases count")
    void save_assignsIdAndIncreasesCount() {
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.SUBMITTED, Complaint.ComplaintPriority.MEDIUM));
        assertEquals(1, repo.count());
    }

    @Test @DisplayName("findById returns saved complaint")
    void findById_returnsSaved() {
        Complaint c = repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.SUBMITTED, Complaint.ComplaintPriority.LOW));
        assertTrue(repo.findById(c.getComplaintId()).isPresent());
    }

    @Test @DisplayName("findByStudentId filters correctly")
    void findByStudentId_filters() {
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.SUBMITTED, Complaint.ComplaintPriority.LOW));
        repo.save(makeComplaint(2, 2, Complaint.ComplaintStatus.SUBMITTED, Complaint.ComplaintPriority.LOW));
        assertEquals(1, repo.findByStudentId(1).size());
    }

    @Test @DisplayName("filterComplaints with ALL status returns all")
    void filterComplaints_allStatus_returnsAll() {
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.SUBMITTED, Complaint.ComplaintPriority.LOW));
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.RESOLVED, Complaint.ComplaintPriority.HIGH));
        assertEquals(2, repo.filterComplaints(null, null, "ALL", "ALL").size());
    }

    @Test @DisplayName("countResolved counts only RESOLVED status")
    void countResolved_onlyResolved() {
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.RESOLVED, Complaint.ComplaintPriority.MEDIUM));
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.SUBMITTED, Complaint.ComplaintPriority.MEDIUM));
        assertEquals(1, repo.countResolved());
    }

    @Test @DisplayName("countPending excludes RESOLVED and CLOSED")
    void countPending_excludesResolvedAndClosed() {
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.SUBMITTED, Complaint.ComplaintPriority.LOW));
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.RESOLVED, Complaint.ComplaintPriority.LOW));
        repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.CLOSED, Complaint.ComplaintPriority.LOW));
        assertEquals(1, repo.countPending());
    }

    @Test @DisplayName("delete removes the complaint")
    void delete_removesComplaint() {
        Complaint c = repo.save(makeComplaint(1, 2, Complaint.ComplaintStatus.SUBMITTED, Complaint.ComplaintPriority.LOW));
        assertTrue(repo.delete(c.getComplaintId()));
        assertEquals(0, repo.count());
    }
}
