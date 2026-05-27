package com.compass.util;

import com.compass.model.Complaint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ComplaintValidator Unit Tests")
class ComplaintValidatorTest {

    private Complaint validComplaint() {
        Complaint c = new Complaint();
        c.setStudentId(1);
        c.setTitle("Broken projector in hall B");
        c.setDescription("The projector has been broken for a week and is affecting lectures.");
        c.setCategory("Facilities");
        c.setPriority(Complaint.ComplaintPriority.MEDIUM);
        c.setAssignedDepartmentId(2);
        return c;
    }

    @Test @DisplayName("Valid complaint produces no errors")
    void validComplaint_noErrors() {
        assertTrue(ComplaintValidator.validate(validComplaint()).isEmpty());
    }

    @Test @DisplayName("Null complaint returns single error")
    void nullComplaint_returnsError() {
        List<String> errors = ComplaintValidator.validate(null);
        assertEquals(1, errors.size());
    }

    @Test @DisplayName("Blank title is rejected")
    void blankTitle_error() {
        Complaint c = validComplaint();
        c.setTitle("   ");
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("Title shorter than minimum is rejected")
    void shortTitle_error() {
        Complaint c = validComplaint();
        c.setTitle("Hi");
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("Title over maximum length is rejected")
    void longTitle_error() {
        Complaint c = validComplaint();
        c.setTitle("A".repeat(201));
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("Blank description is rejected")
    void blankDescription_error() {
        Complaint c = validComplaint();
        c.setDescription("  ");
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("Description shorter than minimum is rejected")
    void shortDescription_error() {
        Complaint c = validComplaint();
        c.setDescription("too short");
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("Null category is rejected")
    void nullCategory_error() {
        Complaint c = validComplaint();
        c.setCategory(null);
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("Null priority is rejected")
    void nullPriority_error() {
        Complaint c = validComplaint();
        c.setPriority(null);
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("Zero studentId is rejected")
    void zeroStudentId_error() {
        Complaint c = validComplaint();
        c.setStudentId(0);
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("Null department is rejected")
    void nullDepartment_error() {
        Complaint c = validComplaint();
        c.setAssignedDepartmentId(null);
        assertFalse(ComplaintValidator.validate(c).isEmpty());
    }

    @Test @DisplayName("validateOrThrow throws for invalid complaint")
    void validateOrThrow_invalid_throws() {
        Complaint c = validComplaint();
        c.setTitle("");
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintValidator.validateOrThrow(c));
    }

    @Test @DisplayName("isValid returns true for a valid complaint")
    void isValid_validComplaint_returnsTrue() {
        assertTrue(ComplaintValidator.isValid(validComplaint()));
    }

    @Test @DisplayName("isValid returns false for an invalid complaint")
    void isValid_invalidComplaint_returnsFalse() {
        Complaint c = validComplaint();
        c.setTitle(null);
        assertFalse(ComplaintValidator.isValid(c));
    }
}
