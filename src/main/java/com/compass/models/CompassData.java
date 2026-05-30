package com.compass.models;

import com.compass.config.AppConfig;
import com.compass.config.Database;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CompassData {
    public static final List<String> CATEGORIES = List.of("Dorm", "Library", "Cafe", "Other");
    private static final Set<String> ALLOWED_UPLOADS = Set.of("pdf", "doc", "docx", "jpg", "jpeg", "png");

    private static final String COMPLAINT_SELECT = """
            SELECT c.*, u.full_name AS student_name
            FROM complaints c
            JOIN users u ON c.student_id = u.user_id
            """;

    public void seedDefaultUsers() {
        if (!Database.isAvailable() || countUsers() > 0) {
            return;
        }
        saveUser(newUser("sysadmin", "sysadmin@aastu.edu.et", "System Admin", "Admin@123", User.UserRole.SYSTEM_ADMIN));
        saveUser(newUser("admin", "admin@aastu.edu.et", "Complaint Admin", "Admin@123", User.UserRole.ADMIN));
        saveUser(newUser("student1", "student1@aastu.edu.et", "Demo Student", "Student@123", User.UserRole.STUDENT));
    }

    public Optional<User> login(String username, String password) {
        Optional<User> user = findUserByUsername(username.trim());
        if (user.isEmpty() || !user.get().isActive() || !BCrypt.checkpw(password, user.get().getPasswordHash())) {
            return Optional.empty();
        }
        return user;
    }

    public User registerStudent(String username, String email, String fullName, String password) {
        validateUserInput(username, email, fullName, password);
        return saveUser(newUser(username.trim(), email.trim(), fullName.trim(), password, User.UserRole.STUDENT));
    }

    public User addAdmin(String username, String email, String fullName, String password) {
        validateUserInput(username, email, fullName, password);
        return saveUser(newUser(username.trim(), email.trim(), fullName.trim(), password, User.UserRole.ADMIN));
    }

    public void removeAdmin(int adminId) {
        String sql = "UPDATE users SET is_active=FALSE WHERE user_id=? AND role='ADMIN'";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, adminId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not remove admin", e);
        }
    }

    public Complaint submitComplaint(Complaint complaint, File attachment) {
        validateComplaint(complaint);
        if (attachment != null) {
            complaint.setAttachmentPath(saveAttachment(attachment, complaint.getStudentId()));
        }

        String sql = """
                INSERT INTO complaints (student_id, title, description, category, status, attachment_path)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, complaint.getStudentId());
            statement.setString(2, complaint.getTitle());
            statement.setString(3, complaint.getDescription());
            statement.setString(4, complaint.getCategory());
            statement.setString(5, Complaint.ComplaintStatus.PENDING.label());
            statement.setString(6, complaint.getAttachmentPath());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    complaint.setComplaintId(keys.getInt(1));
                }
            }
            complaint.setStatus(Complaint.ComplaintStatus.PENDING);
            return complaint;
        } catch (SQLException e) {
            throw new RuntimeException("Could not submit complaint", e);
        }
    }

    public List<Complaint> studentComplaints(int studentId) {
        return complaints(COMPLAINT_SELECT + " WHERE c.student_id=? ORDER BY c.created_at DESC", studentId);
    }

    public List<Complaint> allComplaints() {
        return complaints(COMPLAINT_SELECT + " ORDER BY c.created_at DESC");
    }

    public List<Complaint> filterComplaints(String category, Complaint.ComplaintStatus status) {
        StringBuilder sql = new StringBuilder(COMPLAINT_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (category != null && !"All".equals(category)) {
            sql.append(" AND c.category=?");
            params.add(category);
        }
        if (status != null) {
            sql.append(" AND c.status=?");
            params.add(status.label());
        }
        sql.append(" ORDER BY c.created_at DESC");
        return complaints(sql.toString(), params.toArray());
    }

    public void updateComplaint(int complaintId, Complaint.ComplaintStatus status, String adminNote) {
        String sql = """
                UPDATE complaints
                SET status=?, admin_note=?, resolved_at=?
                WHERE complaint_id=?
                """;
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.label());
            statement.setString(2, adminNote == null ? "" : adminNote.trim());
            if (status == Complaint.ComplaintStatus.RESOLVED) {
                statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                statement.setTimestamp(3, null);
            }
            statement.setInt(4, complaintId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update complaint", e);
        }
    }

    public long totalComplaints() {
        return count("SELECT COUNT(*) FROM complaints");
    }

    public long countByStatus(Complaint.ComplaintStatus status) {
        return count("SELECT COUNT(*) FROM complaints WHERE status='" + status.label() + "'");
    }

    public Map<String, Long> complaintsByCategory() {
        Map<String, Long> totals = new LinkedHashMap<>();
        CATEGORIES.forEach(category -> totals.put(category, 0L));
        String sql = "SELECT category, COUNT(*) AS total FROM complaints GROUP BY category";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                totals.put(rs.getString("category"), rs.getLong("total"));
            }
            return totals;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load category analytics", e);
        }
    }

    public List<User> admins() {
        return users("SELECT * FROM users WHERE role='ADMIN' AND is_active=TRUE ORDER BY full_name");
    }

    public List<User> students(String search) {
        if (search == null || search.isBlank()) {
            return users("SELECT * FROM users WHERE role='STUDENT' AND is_active=TRUE ORDER BY full_name");
        }
        String value = "%" + search.trim() + "%";
        return users("""
                SELECT * FROM users
                WHERE role='STUDENT' AND is_active=TRUE
                  AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?)
                ORDER BY full_name
                """, value, value, value);
    }

    public List<CampusLocation> locations() {
        List<CampusLocation> locations = new ArrayList<>();
        String sql = "SELECT * FROM campus_locations ORDER BY location_id";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                CampusLocation location = new CampusLocation();
                location.setLocationId(rs.getInt("location_id"));
                location.setLocationName(rs.getString("location_name"));
                location.setLatitude(rs.getDouble("latitude"));
                location.setLongitude(rs.getDouble("longitude"));
                locations.add(location);
            }
            return locations;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load campus locations", e);
        }
    }

    public Route route(CampusLocation start, CampusLocation destination) {
        if (start == null || destination == null) {
            return new Route(List.of(), 0);
        }
        if (start.getLocationId() == destination.getLocationId()) {
            return new Route(List.of(start), 0);
        }
        return new Route(List.of(start, destination), distanceMeters(start, destination));
    }

    public String formatRoute(List<CampusLocation> route) {
        return String.join(" -> ", route.stream().map(CampusLocation::getLocationName).toList());
    }

    public String formatRoute(List<Integer> ids, List<CampusLocation> locations) {
        return String.join(" -> ", ids.stream()
                .map(id -> locations.stream()
                        .filter(location -> location.getLocationId() == id)
                        .findFirst()
                        .map(CampusLocation::getLocationName)
                        .orElse("Unknown"))
                .toList());
    }

    private void validateUserInput(String username, String email, String fullName, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (exists("username", username.trim())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (exists("email", email.trim())) {
            throw new IllegalArgumentException("Email already registered");
        }
    }

    private void validateComplaint(Complaint complaint) {
        if (complaint.getStudentId() <= 0) {
            throw new IllegalArgumentException("Login as a student before submitting a complaint");
        }
        if (complaint.getTitle() == null || complaint.getTitle().trim().length() < 5) {
            throw new IllegalArgumentException("Title must be at least 5 characters");
        }
        if (complaint.getDescription() == null || complaint.getDescription().trim().length() < 10) {
            throw new IllegalArgumentException("Description must be at least 10 characters");
        }
        if (!CATEGORIES.contains(complaint.getCategory())) {
            throw new IllegalArgumentException("Choose Dorm, Library, Cafe, or Other");
        }
    }

    private Optional<User> findUserByUsername(String username) {
        List<User> users = users("SELECT * FROM users WHERE username=? AND is_active=TRUE", username);
        return users.stream().findFirst();
    }

    private User saveUser(User user) {
        String sql = """
                INSERT INTO users (username, email, password_hash, full_name, role, is_active)
                VALUES (?, ?, ?, ?, ?, TRUE)
                """;
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getFullName());
            statement.setString(5, user.getRole().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Could not save user", e);
        }
    }

    private User newUser(String username, String email, String fullName, String password, User.UserRole role) {
        User user = new User(username, email, fullName, role);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        return user;
    }

    private boolean exists(String column, String value) {
        String sql = "SELECT 1 FROM users WHERE " + column + "=?";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not check duplicate user", e);
        }
    }

    private List<User> users(String sql, Object... params) {
        List<User> users = new ArrayList<>();
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load users", e);
        }
    }

    private List<Complaint> complaints(String sql, Object... params) {
        List<Complaint> complaints = new ArrayList<>();
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    complaints.add(mapComplaint(rs));
                }
            }
            return complaints;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load complaints", e);
        }
    }

    private void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    private Complaint mapComplaint(ResultSet rs) throws SQLException {
        Complaint complaint = new Complaint();
        complaint.setComplaintId(rs.getInt("complaint_id"));
        complaint.setStudentId(rs.getInt("student_id"));
        complaint.setStudentName(rs.getString("student_name"));
        complaint.setTitle(rs.getString("title"));
        complaint.setDescription(rs.getString("description"));
        complaint.setCategory(rs.getString("category"));
        complaint.setStatus(Complaint.ComplaintStatus.fromLabel(rs.getString("status")));
        complaint.setAttachmentPath(rs.getString("attachment_path"));
        complaint.setAdminNote(rs.getString("admin_note"));
        complaint.setCreatedAt(localDateTime(rs, "created_at"));
        complaint.setResolvedAt(localDateTime(rs, "resolved_at"));
        return complaint;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(localDateTime(rs, "created_at"));
        return user;
    }

    private LocalDateTime localDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private long countUsers() {
        return count("SELECT COUNT(*) FROM users WHERE is_active=TRUE");
    }

    private long count(String sql) {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not count records", e);
        }
    }

    private String saveAttachment(File source, int userId) {
        validateUpload(source);
        try {
            Path uploadDir = Path.of(AppConfig.get("upload.directory", "./uploads"));
            Files.createDirectories(uploadDir);
            String filename = "complaint_" + userId + "_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + "." + extension(source.getName()).toLowerCase();
            Path target = uploadDir.resolve(filename);
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not save attachment", e);
        }
    }

    private void validateUpload(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
        long maxBytes = AppConfig.getInt("upload.max.size.mb", 50) * 1024L * 1024L;
        if (file.length() > maxBytes) {
            throw new IllegalArgumentException("File exceeds maximum size");
        }
        String extension = extension(file.getName()).toLowerCase();
        if (!ALLOWED_UPLOADS.contains(extension)) {
            throw new IllegalArgumentException("Allowed files: pdf, doc, docx, jpg, jpeg, png");
        }
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new IllegalArgumentException("File must have an extension");
        }
        return filename.substring(dot + 1);
    }

    private double distanceMeters(CampusLocation start, CampusLocation destination) {
        double earthRadius = 6_371_000;
        double startLat = Math.toRadians(start.getLatitude());
        double endLat = Math.toRadians(destination.getLatitude());
        double latDelta = Math.toRadians(destination.getLatitude() - start.getLatitude());
        double lonDelta = Math.toRadians(destination.getLongitude() - start.getLongitude());
        double a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2)
                + Math.cos(startLat) * Math.cos(endLat)
                * Math.sin(lonDelta / 2) * Math.sin(lonDelta / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public record Route(List<CampusLocation> locations, double totalDistanceMeters) {}
}
