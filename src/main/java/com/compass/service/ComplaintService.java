package com.compass.service;

import com.compass.model.Complaint;
import com.compass.model.ComplaintTransfer;
import com.compass.repository.ComplaintRepository;

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
        routeComplaint(complaint);
        Complaint saved = complaintRepository.save(complaint);
        return saved;
    }

    public void validateComplaint(Complaint complaint) {
        List<String> errors = new ArrayList<>();
        if (complaint.getTitle() == null || complaint.getTitle().trim().isEmpty()) {
            errors.add("Complaint title is required.");
        }
        if (complaint.getDescription() == null || complaint.getDescription().trim().length() < 10) {
            errors.add("Complaint description must be at least 10 characters.");
        }
        if (complaint.getStudentId() <= 0) {
            errors.add("A valid student identifier is required.");
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }

    public void routeComplaint(Complaint complaint) {
        if (complaint.getDepartmentId() != null) {
            return;
        }

        String description = complaint.getDescription().toLowerCase();
        if (description.contains("wifi") || description.contains("network") || description.contains("internet") || description.contains("email")) {
            complaint.setDepartmentId(2);
        } else if (description.contains("housing") || description.contains("room") || description.contains("maintenance") || description.contains("electric")) {
            complaint.setDepartmentId(1);
        } else {
            complaint.setDepartmentId(3);
        }
    }

    public Optional<Complaint> findById(int complaintId) throws SQLException {
        return complaintRepository.findById(complaintId);
    }

    public List<Complaint> findStudentComplaints(int studentId) throws SQLException {
        return complaintRepository.findByStudentId(studentId);
    }

    public List<Complaint> findByStatus(String status) throws SQLException {
        return complaintRepository.findByStatus(status);
    }

    public List<Complaint> searchComplaints(String query) throws SQLException {
        return complaintRepository.searchComplaints(query);
    }

    public Complaint updateComplaintStatus(int complaintId, String status) throws SQLException {
        Optional<Complaint> complaintOptional = complaintRepository.findById(complaintId);
        if (complaintOptional.isEmpty()) {
            throw new SQLException("Complaint not found: " + complaintId);
        }
        Complaint complaint = complaintOptional.get();
        complaint.setStatus(status);
        return complaintRepository.save(complaint);
    }

    public Complaint transferComplaint(int complaintId, int departmentId, String reason) throws SQLException {
        return complaintRepository.transferComplaint(complaintId, departmentId, reason);
    }

    public List<ComplaintTransfer> getComplaintHistory(int complaintId) throws SQLException {
        return complaintRepository.getComplaintHistory(complaintId);
    }

    public Task<List<Complaint>> loadStudentComplaintsTask(int studentId) {
        return new Task<>() {
            @Override
            protected List<Complaint> call() throws Exception {
                return findStudentComplaints(studentId);
            }
        };
    }
}
