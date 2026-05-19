package com.compass.service;

import com.compass.model.Attachment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileUploadUtil {
    public static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif");
    public static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt");
    public static final long DEFAULT_MAX_BYTES = 10L * 1024L * 1024L;

    private FileUploadUtil() {
    }

    public static boolean isValidFileType(Path file, Set<String> allowedExtensions) {
        String fileName = file.getFileName().toString();
        String extension = getExtension(fileName);
        return allowedExtensions.contains(extension.toLowerCase());
    }

    public static boolean isWithinSizeLimit(Path file, long maxBytes) throws IOException {
        return Files.size(file) <= maxBytes;
    }

    public static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    public static Path storeAttachment(Path source, Path uploadDir) throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        String fileName = generateSafeFileName(source);
        Path target = uploadDir.resolve(fileName);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    public static List<Attachment> processAttachments(List<Path> selectedFiles, Path uploadDir) throws IOException {
        List<Attachment> attachments = new ArrayList<>();
        Set<String> allowedTypes = new HashSet<>();
        allowedTypes.addAll(IMAGE_EXTENSIONS);
        allowedTypes.addAll(DOCUMENT_EXTENSIONS);

        for (Path file : selectedFiles) {
            if (!Files.exists(file) || !isValidFileType(file, allowedTypes)) {
                throw new IllegalArgumentException("Unsupported file type: " + file.getFileName());
            }
            if (!isWithinSizeLimit(file, DEFAULT_MAX_BYTES)) {
                throw new IllegalArgumentException("File exceeds maximum allowed size: " + file.getFileName());
            }
            Path stored = storeAttachment(file, uploadDir);
            Attachment attachment = new Attachment();
            attachment.setFileName(stored.getFileName().toString());
            attachment.setFilePath(stored.toAbsolutePath().toString());
            attachment.setFileType(getExtension(stored.getFileName().toString()));
            attachment.setFileSize(Files.size(stored));
            attachment.setUploadedAt(LocalDateTime.now());
            attachments.add(attachment);
        }
        return attachments;
    }

    private static String generateSafeFileName(Path source) {
        String original = source.getFileName().toString();
        String extension = getExtension(original);
        String baseName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (baseName.length() > 120) {
            baseName = baseName.substring(0, 120);
        }
        return System.currentTimeMillis() + "_" + baseName + (extension.isEmpty() ? "" : "");
    }
}
