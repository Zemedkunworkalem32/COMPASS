package com.compass.service;

import com.compass.model.Complaint;
import com.compass.repository.ComplaintRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ComplaintServiceTest {
    @Test
    public void validateComplaint_shouldAllowValidComplaint() {
        Complaint complaint = new Complaint();
        complaint.setTitle("Broken classroom projector");
        complaint.setDescription("Projector at Oak Hall room 101 does not power on.");
        complaint.setStudentId(1);

        ComplaintService service = new ComplaintService(new ComplaintRepository(true) {
            @Override
            public Optional<Complaint> findById(int id) {
                return Optional.empty();
            }

            @Override
            public List<Complaint> findAll() {
                return Collections.emptyList();
            }

            @Override
            public Complaint save(Complaint entity) {
                return entity;
            }

            @Override
            public void delete(int id) {
            }
        });

        Assertions.assertDoesNotThrow(() -> service.validateComplaint(complaint));
    }

    @Test
    public void validateComplaint_shouldRejectShortDescription() {
        Complaint complaint = new Complaint();
        complaint.setTitle("Broken chair");
        complaint.setDescription("Too short");
        complaint.setStudentId(1);

        ComplaintService service = new ComplaintService(new ComplaintRepository(true) {
            @Override
            public Optional<Complaint> findById(int id) {
                return Optional.empty();
            }

            @Override
            public List<Complaint> findAll() {
                return Collections.emptyList();
            }

            @Override
            public Complaint save(Complaint entity) {
                return entity;
            }

            @Override
            public void delete(int id) {
            }
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> service.validateComplaint(complaint));
    }
}
