package com.compass.util;

import com.compass.model.Complaint;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates complaint input fields before persistence.
 */
public final class ComplaintValidator {

    private static final int TITLE_MIN       = 5;
    private static final int TITLE_MAX       = 200;
    private static final int DESCRIPTION_MIN = 10;
    private static final int DESCRIPTION_MAX = 5000;

    private ComplaintValidator() {}

    /**
     * Validates a complaint and returns a list of human-readable error messages.
     * An empty list means the complaint is valid.
     */
    public static List<String> validate(Complaint complaint) {
        List<String> errors = new ArrayList<>();

        if (complaint == null) {
            errors.add("Complaint must not be null");
            return errors;
        }

        // Title
        String title = complaint.getTitle();
        if (title == null || title.isBlank()) {
            errors.add("Title is required");
        } else if (title.trim().length() < TITLE_MIN) {
            errors.add("Title must be at least " + TITLE_MIN + " characters");
        } else if (title.trim().length() > TITLE_MAX) {
            errors.add("Title must not exceed " + TITLE_MAX + " characters");
        }

        // Description
        String desc = complaint.getDescription();
        if (desc == null || desc.isBlank()) {
            errors.add("Description is required");
        } else if (desc.trim().length() < DESCRIPTION_MIN) {
            errors.add("Description must be at least " + DESCRIPTION_MIN + " characters");
        } else if (desc.trim().length() > DESCRIPTION_MAX) {
            errors.add("Description must not exceed " + DESCRIPTION_MAX + " characters");
        }

        // Category
        if (complaint.getCategory() == null || complaint.getCategory().isBlank()) {
            errors.add("Category is required");
        }

        // Priority
        if (complaint.getPriority() == null) {
            errors.add("Priority is required");
        }

        // Student ID
        if (complaint.getStudentId() <= 0) {
            errors.add("A valid student ID is required");
        }

        // Department
        if (complaint.getAssignedDepartmentId() == null || complaint.getAssignedDepartmentId() <= 0) {
            errors.add("A department must be selected");
        }

        return errors;
    }

    /**
     * Throws {@link IllegalArgumentException} with the first validation error if any exist.
     */
    public static void validateOrThrow(Complaint complaint) {
        List<String> errors = validate(complaint);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0));
        }
    }

    /**
     * Returns true only when the complaint passes all validation rules.
     */
    public static boolean isValid(Complaint complaint) {
        return validate(complaint).isEmpty();
    }
}
