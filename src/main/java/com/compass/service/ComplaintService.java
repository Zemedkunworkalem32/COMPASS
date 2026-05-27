package com.compass.service;

import com.compass.model.Complaint;
import com.compass.repository.ComplaintRepository;
import com.compass.util.FileUploadUtil;
import com.compass.util.RemoteReportingClient;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Complaint submission, assignment, filtering, and status updates.
 */
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
    private final RemoteReportingClient reportingClient;

    public ComplaintService() {
        this(RepositoryFactory.complaints(), new RemoteReportingClient());
    }

    public ComplaintService(ComplaintRepository complaintRepository, RemoteReportingClient reportingClient) {
        this.complaintRepository = complaintRepository;
        this.reportingClient = reportingClient;
    }

    public Complaint submitComplaint(Complaint complaint, File attachment) {
        if (attachment != null) {
            String path = FileUploadUtil.saveAttachment(attachment, complaint.getStudentId());
            complaint.setAttachmentPath(path);
        }
        complaint.setStatus(Complaint.ComplaintStatus.SUBMITTED);
        Complaint saved = complaintRepository.save(complaint);
        reportingClient.reportComplaint(saved);
        return saved;
    }

    public List<Complaint> getStudentComplaints(int studentId) {
        return complaintRepository.findByStudentId(studentId);
    }

    public List<Complaint> getDepartmentComplaints(int departmentId) {
        return complaintRepository.findByDepartmentId(departmentId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public List<Complaint> filter(Integer departmentId, String status, String priority) {
        return complaintRepository.filterComplaints(null, departmentId, status, priority);
    }

    public Complaint assignToDepartment(int complaintId, int departmentId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        complaint.setAssignedDepartmentId(departmentId);
        complaint.setStatus(Complaint.ComplaintStatus.ASSIGNED);
        return complaintRepository.update(complaint);
    }

    public Complaint markUnderReview(int complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        complaint.setStatus(Complaint.ComplaintStatus.UNDER_REVIEW);
        return complaintRepository.update(complaint);
    }

    public Complaint updateStatus(int complaintId, Complaint.ComplaintStatus status, String notes) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        complaint.setStatus(status);
        if (notes != null && !notes.isBlank()) {
            complaint.setResolutionNotes(notes);
        }
        if (status == Complaint.ComplaintStatus.RESOLVED || status == Complaint.ComplaintStatus.CLOSED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        return complaintRepository.update(complaint);
    }

    public Complaint resolveComplaint(int complaintId, String resolutionNotes) {
        return updateStatus(complaintId, Complaint.ComplaintStatus.RESOLVED, resolutionNotes);
    }
}
