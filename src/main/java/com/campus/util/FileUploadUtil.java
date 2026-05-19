package com.campus.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

public class FileUploadUtil {

    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "gif", "bmp");
    public static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of("pdf", "doc", "docx", "txt", "rtf");
    public static final long MAX_FILE_SIZE_MB = 10;
    public static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    
    private static final String UPLOAD_BASE_DIR = "uploads";

    private FileUploadUtil() {}

    public static boolean isValidFileType(String fileName, Set<String> allowedTypes) {
        String extension = getFileExtension(fileName);
        return allowedTypes.contains(extension.toLowerCase());
    }

    public static boolean isValidFileSize(Path filePath, long maxBytes) throws IOException {
        return Files.size(filePath) <= maxBytes;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public static Path storeFile(Path sourceFile, int complaintId, String originalFileName) throws IOException {
        String uploadDirPath = UPLOAD_BASE_DIR + "/complaint_" + complaintId;
        Path uploadDir = Paths.get(uploadDirPath);
        
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        String extension = getFileExtension(originalFileName);
        String safeFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        if (!extension.isEmpty()) {
            safeFileName += "." + extension;
        }
        
        Path targetPath = uploadDir.resolve(safeFileName);
        Files.copy(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return targetPath;
    }

    public static boolean deleteComplaintFiles(int complaintId) throws IOException {
        String uploadDirPath = UPLOAD_BASE_DIR + "/complaint_" + complaintId;
        Path uploadDir = Paths.get(uploadDirPath);
        
        if (Files.exists(uploadDir)) {
            Files.walk(uploadDir)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            return true;
        }
        return false;
    }
}