package com.compass.service;

import com.compass.model.Complaint;
import com.compass.repository.ComplaintRepository;
import com.compass.util.RemoteReportingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplaintService Unit Tests")
class ComplaintServiceTest {

    @Mock private ComplaintRepository complaintRepository;
    @Mock private RemoteReportingClient reportingClient;

    private ComplaintService complaintService;

    @BeforeEach
    void setUp() {
        complaintService = new ComplaintService(complaintRepository, reportingClient);
    }

    // ── submitComplaint ────────────────────────────────────────────────────────

    @Test
    @DisplayName("submitComplaint sets SUBMITTED status and calls repository.save")
    void submitComplaint_setsStatusAndSaves() {
        Complaint complaint = buildComplaint();
        when(complaintRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reportingClient.reportComplaint(any())).thenReturn(true);

        Complaint result = complaintService.submitComplaint(complaint, null);

        assertEquals(Complaint.ComplaintStatus.SUBMITTED, result.getStatus());
        verify(complaintRepository).save(complaint);
        verify(reportingClient).reportComplaint(complaint);
    }

    @Test
    @DisplayName("submitComplaint with null attachment does not set attachmentPath")
    void submitComplaint_nullAttachment_noPath() {
        Complaint complaint = buildComplaint();
        when(complaintRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reportingClient.reportComplaint(any())).thenReturn(true);

        Complaint result = complaintService.submitComplaint(complaint, null);

        assertNull(result.getAttachmentPath());
    }

    // ── getStudentComplaints ───────────────────────────────────────────────────

    @Test
    @DisplayName("getStudentComplaints delegates to repository")
    void getStudentComplaints_delegatesToRepository() {
        List<Complaint> expected = List.of(buildComplaint());
        when(complaintRepository.findByStudentId(1)).thenReturn(expected);

        List<Complaint> actual = complaintService.getStudentComplaints(1);

        assertEquals(expected, actual);
        verify(complaintRepository).findByStudentId(1);
    }

    // ── getDepartmentComplaints ────────────────────────────────────────────────

    @Test
    @DisplayName("getDepartmentComplaints delegates to repository")
    void getDepartmentComplaints_delegatesToRepository() {
        List<Complaint> expected = List.of(buildComplaint());
        when(complaintRepository.findByDepartmentId(2)).thenReturn(expected);

        List<Complaint> actual = complaintService.getDepartmentComplaints(2);

        assertEquals(expected, actual);
        verify(complaintRepository).findByDepartmentId(2);
    }

    // ── assignToDepartment ────────────────────────────────────────────────────

    @Test
    @DisplayName("assignToDepartment sets department and ASSIGNED status")
    void assignToDepartment_updatesStatusAndDept() {
        Complaint complaint = buildComplaint();
        when(complaintRepository.findById(1)).thenReturn(Optional.of(complaint));
        when(complaintRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Complaint result = complaintService.assignToDepartment(1, 3);

        assertEquals(Complaint.ComplaintStatus.ASSIGNED, result.getStatus());
        assertEquals(3, result.getAssignedDepartmentId());
    }

    @Test
    @DisplayName("assignToDepartment throws when complaint not found")
    void assignToDepartment_notFound_throws() {
        when(complaintRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> complaintService.assignToDepartment(99, 1));
    }

    // ── resolveComplaint ──────────────────────────────────────────────────────

    @Test
    @DisplayName("resolveComplaint sets RESOLVED status and resolvedAt timestamp")
    void resolveComplaint_setsStatusAndTimestamp() {
        Complaint complaint = buildComplaint();
        when(complaintRepository.findById(1)).thenReturn(Optional.of(complaint));
        when(complaintRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Complaint result = complaintService.resolveComplaint(1, "All sorted");

        assertEquals(Complaint.ComplaintStatus.RESOLVED, result.getStatus());
        assertNotNull(result.getResolvedAt());
        assertEquals("All sorted", result.getResolutionNotes());
    }

    // ── markUnderReview ───────────────────────────────────────────────────────

    @Test
    @DisplayName("markUnderReview sets UNDER_REVIEW status")
    void markUnderReview_setsStatus() {
        Complaint complaint = buildComplaint();
        when(complaintRepository.findById(1)).thenReturn(Optional.of(complaint));
        when(complaintRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Complaint result = complaintService.markUnderReview(1);

        assertEquals(Complaint.ComplaintStatus.UNDER_REVIEW, result.getStatus());
    }

    // ── filter ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("filter passes null studentId and delegates criteria to repository")
    void filter_delegatesFilterCriteria() {
        when(complaintRepository.filterComplaints(null, 2, "SUBMITTED", "HIGH"))
                .thenReturn(List.of());

        complaintService.filter(2, "SUBMITTED", "HIGH");

        verify(complaintRepository).filterComplaints(null, 2, "SUBMITTED", "HIGH");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Complaint buildComplaint() {
        Complaint c = new Complaint();
        c.setComplaintId(1);
        c.setStudentId(1);
        c.setTitle("Broken projector");
        c.setDescription("The projector in room 101 has been broken for a week.");
        c.setCategory("Facilities");
        c.setPriority(Complaint.ComplaintPriority.MEDIUM);
        c.setStatus(Complaint.ComplaintStatus.SUBMITTED);
        c.setAssignedDepartmentId(2);
        return c;
    }
}
