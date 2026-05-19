package com.campus.service;

import com.campus.model.Complaint;
import com.campus.model.ComplaintTransfer;
import com.campus.repository.ComplaintRepository;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintService() throws SQLException {
        this.complaintRepository = new ComplaintRepository();
    }

    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public Complaint createComplaint(Complaint complaint) throws SQLException {
        validateComplaint(complaint);
        autoRouteComplaint(complaint);
        if (complaint.getStatus() == null) {
            complaint.setStatus("PENDING");
        }
        if (complaint.getPriority() == null) {
            complaint.setPriority("MEDIUM");
        }
        return complaintRepository.save(complaint);
    }

    public void validateComplaint(Complaint complaint) {
        List<String> errors = new ArrayList<>();
        
        if (complaint.getTitle() == null || complaint.getTitle().trim().isEmpty()) {
            errors.add("Complaint title is required.");
        } else if (complaint.getTitle().length() < 5) {
            errors.add("Complaint title must be at least 5 characters.");
        } else if (complaint.getTitle().length() > 200) {
            errors.add("Complaint title must not exceed 200 characters.");
        }
        
        if (complaint.getDescription() == null || complaint.getDescription().trim().isEmpty()) {
            errors.add("Complaint description is required.");
        } else if (complaint.getDescription().length() < 10) {
            errors.add("Complaint description must be at least 10 characters.");
        } else if (complaint.getDescription().length() > 2000) {
            errors.add("Complaint description must not exceed 2000 characters.");
        }
        
        if (complaint.getStudentId() <= 0) {
            errors.add("Valid student ID is required.");
        }
        
        String priority = complaint.getPriority();
        if (priority != null && !priority.matches("LOW|MEDIUM|HIGH|URGENT")) {
            errors.add("Priority must be LOW, MEDIUM, HIGH, or URGENT.");
        }
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }

    public void autoRouteComplaint(Complaint complaint) {
        if (complaint.getDepartmentId() != null && complaint.getDepartmentId() > 0) {
            return;
        }
        
        String title = complaint.getTitle().toLowerCase();
        String description = complaint.getDescription().toLowerCase();
        String combined = title + " " + description;
        
        // IT Services (Department ID: 2)
        if (combined.contains("wifi") || combined.contains("network") || combined.contains("internet") ||
            combined.contains("computer") || combined.contains("software") || combined.contains("email") ||
            combined.contains("printer") || combined.contains("login") || combined.contains("password")) {
            complaint.setDepartmentId(2);
        }
        // Facilities Management (Department ID: 5)
        else if (combined.contains("maintenance") || combined.contains("repair") || combined.contains("broken") ||
                 combined.contains("cleaning") || combined.contains("electricity") || combined.contains("power") ||
                 combined.contains("water") || combined.contains("ac") || combined.contains("heating") ||
                 combined.contains("furniture") || combined.contains("window") || combined.contains("door")) {
            complaint.setDepartmentId(5);
        }
        // Academic Affairs (Department ID: 1)
        else if (combined.contains("course") || combined.contains("class") || combined.contains("teacher") ||
                 combined.contains("professor") || combined.contains("exam") || combined.contains("grade") ||
                 combined.contains("registration") || combined.contains("transcript") || combined.contains("syllabus")) {
            complaint.setDepartmentId(1);
        }
        // Library (Department ID: 3)
        else if (combined.contains("book") || combined.contains("library") || combined.contains("study room") ||
                 combined.contains("journal") || combined.contains("resource")) {
            complaint.setDepartmentId(3);
        }
        // Student Affairs (Department ID: 4)
        else if (combined.contains("housing") || combined.contains("dorm") || combined.contains("counseling") ||
                 combined.contains("student activity") || combined.contains("discipline") || combined.contains("event")) {
            complaint.setDepartmentId(4);
        }
        // Finance (Department ID: 6)
        else if (combined.contains("fee") || combined.contains("payment") || combined.contains("scholarship") ||
                 combined.contains("tuition") || combined.contains("refund") || combined.contains("bill")) {
            complaint.setDepartmentId(6);
        }
        // Default to Student Affairs
        else {
            complaint.setDepartmentId(4);
        }
    }

    public Optional<Complaint> findById(int complaintId) throws SQLException {
        return complaintRepository.findById(complaintId);
    }

    public List<Complaint> findAll() throws SQLException {
        return complaintRepository.findAll();
    }

    public List<Complaint> findStudentComplaints(int studentId) throws SQLException {
        return complaintRepository.findByStudentId(studentId);
    }

    public List<Complaint> findDepartmentComplaints(int departmentId) throws SQLException {
        return complaintRepository.findByDepartmentId(departmentId);
    }

    public List<Complaint> findByStatus(String status) throws SQLException {
        return complaintRepository.findByStatus(status);
    }

    public List<Complaint> searchComplaints(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return complaintRepository.findAll();
        }
        return complaintRepository.searchComplaints(query);
    }

    public List<Complaint> filterComplaints(String department, String status, String priority, String dateFrom) throws SQLException {
        return complaintRepository.filterComplaints(department, status, priority, dateFrom);
    }

    public Complaint updateStatus(int complaintId, String status) throws SQLException {
        Optional<Complaint> optional = complaintRepository.findById(complaintId);
        if (optional.isEmpty()) {
            throw new SQLException("Complaint not found: " + complaintId);
        }
        
        Complaint complaint = optional.get();
        String oldStatus = complaint.getStatus();
        complaint.setStatus(status);
        
        complaintRepository.updateStatus(complaintId, status);
        
        // If moving to IN_PROGRESS, record first response time
        if ("IN_PROGRESS".equals(status) && !"IN_PROGRESS".equals(oldStatus)) {
            complaintRepository.markFirstResponse(complaintId);
        }
        
        // If moving to RESOLVED, record resolution time
        if ("RESOLVED".equals(status)) {
            complaintRepository.markAsResolved(complaintId);
        }
        
        return complaint;
    }

    public Complaint transferComplaint(int complaintId, int departmentId, String reason) throws SQLException {
        return complaintRepository.transferComplaint(complaintId, departmentId, reason);
    }

    public List<ComplaintTransfer> getTransferHistory(int complaintId) throws SQLException {
        return complaintRepository.getTransferHistory(complaintId);
    }

    public long countByStatus(String status) throws SQLException {
        return complaintRepository.countByStatus(status);
    }

    public Task<Complaint> submitComplaintTask(Complaint complaint) {
        return new Task<>() {
            @Override
            protected Complaint call() throws Exception {
                return createComplaint(complaint);
            }
        };
    }

    public Task<List<Complaint>> loadStudentComplaintsTask(int studentId) {
        return new Task<>() {
            @Override
            protected List<Complaint> call() throws Exception {
                return findStudentComplaints(studentId);
            }
        };
    }

    public Task<List<Complaint>> loadDepartmentComplaintsTask(int departmentId) {
        return new Task<>() {
            @Override
            protected List<Complaint> call() throws Exception {
                return findDepartmentComplaints(departmentId);
            }
        };
    }
}