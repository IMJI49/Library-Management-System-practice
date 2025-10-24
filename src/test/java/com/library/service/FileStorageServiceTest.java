package com.library.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
public class FileStorageServiceTest {
    @Autowired
    private FileStorageService fileStorageService;
//    private static final long MAX_FILE_SIZE = 1024 * 1024 * 10;

    @Test
    @DisplayName("정상 파일 업로드")
    void testValidateFile_ValidJpa(){
        byte[] content = "test pdf content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.pdf","application/pdf",content);
        fileStorageService.validateFile(file);
    }
}
