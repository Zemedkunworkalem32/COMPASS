package com.campus.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class FileUploadUtilTest {
    @Test
    public void getExtension_shouldReturnCorrectExtension() {
        String extension = FileUploadUtil.getExtension("report.pdf");
        Assertions.assertEquals("pdf", extension);
    }

    @Test
    public void isValidFileType_shouldAcceptImageExtensions() {
        Path dummy = Path.of("photo.jpg");
        Assertions.assertTrue(FileUploadUtil.isValidFileType(dummy, FileUploadUtil.IMAGE_EXTENSIONS));
    }
}

