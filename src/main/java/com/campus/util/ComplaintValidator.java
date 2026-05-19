package com.campus.util;

import com.campus.model.Complaint;

import java.util.ArrayList;
import java.util.List;

public class ComplaintValidator {

    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private ComplaintValidator() {}

    public static List<String> validate(Complaint complaint) {
        List<String> errors = new ArrayList<>();
        
        if (complaint == null) {
            errors.add("Complaint cannot be null");
            return errors;
        }
        
        validateTitle(complaint.getTitle(), errors);
        validateDescription(complaint.getDescription(), errors);
        validateStudentId(complaint.getStudentId(), errors);
        validatePriority(complaint.getPriority(), errors);
        
        return errors;
    }

    public static boolean isValid(Complaint complaint) {
        return validate(complaint).isEmpty();
    }

    private static void validateTitle(String title, List<String> errors) {
        if (title == null || title.trim().isEmpty()) {
            errors.add("Complaint title is required");
        } else if (title.length() < MIN_TITLE_LENGTH) {
           package com.campus.util;

import com.campus.model.Complaint;

import java.util.ArrayList;
import java.util.List;

public class ComplaintValidator {

    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private ComplaintValidator() {}

    public static List<String> validate(Complaint complaint) {
        List<String> errors = new ArrayList<>();
        
        if (complaint == null) {
            errors.add("Complaint cannot be null");
            return errors;
        }
        
        validateTitle(complaint.getTitle(), errors);
        validateDescription(complaint.getDescription(), errors);
        validateStudentId(complaint.getStudentId(), errors);
        validatePriority(complaint.getPriority(), errors);
        
        return errors;
    }

    public static boolean isValid(Complaint complaint) {
        return validate(complaint).isEmpty();
    }

    private static void validateTitle(String title, List<String> errors) {
        if (title == null || title.trim().isEmpty()) {
            errors.add("Complaint title is required");
        } else if (title.length() < MIN_TITLE_LENGTH) {
            errors.add("Complaint title must be at least " + MIN_TITLE_LENGTH + " characters");
        } else if (title.length() > MAX_TITLE_LENGTH) {
            errors.add("Complaint title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
    }

    private static void validateDescription(String description, List<String> errors) {
        if (description == null || description.trim().isEmpty()) {
            errors.add("Complaint description is required");
        } else if (description.length() < MIN_DESCRIPTION_LENGTH) {
            errors.add("Complaint description must be at least " + MIN_DESCRIPTION_LENGTH + " characters");
        } else if (description.length() > MAX_DESCRIPTION_LENGTH) {
            errors.add("Complaint description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }

    private static void validateStudentId(int studentId, List<String> errors) {
        if (studentId <= 0) {
            errors.add("Valid student ID is required");
        }
    }

    private static void validatePriority(String priority, List<String> errors) {
        if (priority == null) return;
        String upperPriority = priority.toUpperCase();
        if (!upperPriority.matches("LOW|MEDIUM|HIGH|URGENT")) {
            errors.add("Priority must be LOW, MEDIUM, HIGH, or URGENT");
        }
    }

    public static String getValidationMessage(Complaint complaint) {
        List<String> errors = validate(complaint);
        return String.join("\n", errors);
    }
}