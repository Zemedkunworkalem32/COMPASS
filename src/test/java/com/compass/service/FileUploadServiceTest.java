package com.compass.service;

import com.compass.util.FileUploadUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileUploadUtil Unit Tests")
class FileUploadServiceTest {

    @TempDir
    Path tempDir;

    // ── validateFile ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("validateFile accepts a valid PDF")
    void validateFile_validPdf_noException() throws IOException {
        File pdf = tempDir.resolve("test.pdf").toFile();
        Files.writeString(pdf.toPath(), "%PDF-1.4 sample content");

        assertDoesNotThrow(() -> FileUploadUtil.validateFile(pdf));
    }

    @Test
    @DisplayName("validateFile accepts jpg, png, docx extensions")
    void validateFile_acceptedExtensions() throws IOException {
        for (String ext : new String[]{"jpg", "jpeg", "png", "doc", "docx", "xls", "xlsx", "gif"}) {
            File f = tempDir.resolve("file." + ext).toFile();
            Files.writeString(f.toPath(), "content");
            assertDoesNotThrow(() -> FileUploadUtil.validateFile(f),
                    "Should accept extension: " + ext);
        }
    }

    @Test
    @DisplayName("validateFile rejects a disallowed extension")
    void validateFile_disallowedExtension_throws() throws IOException {
        File exe = tempDir.resolve("malware.exe").toFile();
        Files.writeString(exe.toPath(), "MZ");

        assertThrows(IllegalArgumentException.class,
                () -> FileUploadUtil.validateFile(exe));
    }

    @Test
    @DisplayName("validateFile rejects null input")
    void validateFile_nullFile_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FileUploadUtil.validateFile(null));
    }

    @Test
    @DisplayName("validateFile rejects a non-existent file")
    void validateFile_nonExistentFile_throws() {
        File ghost = new File("/tmp/does-not-exist-999.pdf");
        assertThrows(IllegalArgumentException.class,
                () -> FileUploadUtil.validateFile(ghost));
    }

    @Test
    @DisplayName("validateFile rejects file with no extension")
    void validateFile_noExtension_throws() throws IOException {
        File noExt = tempDir.resolve("README").toFile();
        Files.writeString(noExt.toPath(), "text");

        assertThrows(IllegalArgumentException.class,
                () -> FileUploadUtil.validateFile(noExt));
    }
}
