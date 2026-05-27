package com.compass.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Validates and stores complaint attachment files.
 */
public final class FileUploadUtil {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "gif"
    );

    private FileUploadUtil() {}

    public static String saveAttachment(java.io.File source, int userId) {
        validateFile(source);
        try {
            Path uploadDir = Path.of(AppConfig.get("upload.directory", "./uploads"));
            Files.createDirectories(uploadDir);
            String ext = getExtension(source.getName());
            String fileName = "complaint_" + userId + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                    "." + ext;
            Path target = uploadDir.resolve(fileName);
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save attachment", e);
        }
    }

    public static void validateFile(java.io.File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
        long maxBytes = AppConfig.getInt("upload.max.size.mb", 50) * 1024L * 1024L;
        if (file.length() > maxBytes) {
            throw new IllegalArgumentException("File exceeds maximum size");
        }
        String ext = getExtension(file.getName());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed: " + ext);
        }
    }

    private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("File must have an extension");
        }
        return fileName.substring(dot + 1);
    }
}
