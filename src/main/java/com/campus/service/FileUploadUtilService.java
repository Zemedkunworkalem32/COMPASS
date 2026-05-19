package com.campus.service;

import com.campus.model.Attachment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FileUploadService {

    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "gif", "bmp");
    public static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of("pdf", "doc", "docx", "txt", "rtf");
    public static final Set<String> ALLOWED_ALL_TYPES = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp", "pdf", "doc", "docx", "txt", "rtf"
    );
    
    public static final long MAX_FILE_SIZE_MB = 10;
    public static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    
    private static final String UPLOAD_BASE_DIR = "uploads";

    private FileUploadService() {}

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

    public static List<Attachment> processAttachments(List<Path> selectedFiles, int complaintId) throws IOException {
        List<Attachment> attachments = new ArrayList<>();
        
        for (Path file : selectedFiles) {
            String fileName = file.getFileName().toString();
            
            if (!isValidFileType(fileName, ALLOWED_ALL_TYPES)) {
                throw new IllegalArgumentException("Unsupported file type: " + fileName);
            }
            
            if (!isValidFileSize(file, MAX_FILE_SIZE_BYTES)) {
                throw new IllegalArgumentException("File exceeds " + MAX_FILE_SIZE_MB + "MB limit: " + fileName);
            }
            
            Path storedPath = storeFile(file, complaintId, fileName);
            
            Attachment attachment = new Attachment();
            attachment.setFileName(fileName);
            attachment.setFileType(getFileExtension(fileName));
            attachment.setFileSize(Files.size(storedPath));
            attachment.setFilePath(storedPath.toString());
            attachment.setUploadedAt(LocalDateTime.now());
            
            attachments.add(attachment);
        }
        
        return attachments;
    }

    public static byte[] readFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public static boolean deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            return true;
        }
        return false;
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