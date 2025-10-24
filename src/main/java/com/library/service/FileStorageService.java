package com.library.service;

import com.library.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
    파일 저장 service - 파일 관리 기능 제공
        - 주요 기능
            - 파일 검증
            - 파일 저장
            - 파일 다운로드
            - 파일 삭제
    @Value 어노테이션
 */
@Service
@Slf4j
public class FileStorageService {
    private final Path uploadPath;
    private final long maxFileSize;
    private final Set<String> allowedExtensions;

    public FileStorageService(@Value("${file.upload-dir}") String uploadPath,@Value("${file.max-size}") long maxFileSize,@Value("${file.allowed-extensions}") String[] allowedExtensions) {
        this.uploadPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        this.maxFileSize =  maxFileSize;
        this.allowedExtensions = new HashSet<>(Arrays.asList(allowedExtensions));
        try {
			Files.createDirectories(this.uploadPath);
            log.info("파일 저장 디렉토리 생성 완료 : {}", this.uploadPath);
            log.info("파일 크기 제한 : {} bytes ({} MB)", this.maxFileSize, maxFileSize/1024/1024);
            log.info("허용된 확장자 : {}", this.allowedExtensions);
		} catch (Exception e) {
			log.error("파일 저장 디렉토리 실패",e);
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.",e);
		}
    }
    // 파일 검증 - 확장자, 크기, 파일명 등 검증하여 보안 위험 차단
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("파일이 비었습니다.");
        }
        if (file.getSize() > maxFileSize) {
            throw new InvalidFileException(String.format("파일 크기가 너무 큽니다.(최대 : %d MB, 현재 : %f MB)", maxFileSize/1024/1024, file.getSize()/1024f/1024f));
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||  originalFilename.trim().isEmpty()) {
            throw new InvalidFileException("파일명이 올바르지 않습니다");
        }
        // 파일 확장자 추출 및 검증
        String extension = getFileExtension(originalFilename).toLowerCase().trim();
        if (extension.isEmpty()) {
            throw new InvalidFileException("파일 확장자가 없습니다.");
        }
        // 허용된 확장자 목록에 있는지 확인
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new InvalidFileException(String.format("허용되지 않은 파일 형식입니다. (허용 : %s, 현재 %s)", allowedExtensions, extension));
        }
        log.debug("파일 검증 성공 {}, (크기 : {}, bytes, 확장자 {}", originalFilename, file.getSize(), extension);
    }
    /*
            파일 저장 -- UUID 파일명 생성 및 날짜별 폴더 구조로 저장
            저장 프로세스
                - 파일 검증(validateFile())
                - UUID 생성하고 고유한 파일명 만들기
                - 날짜별 폴더 경로 생성(yyyy/MM/dd 형식)
                - 파일 저장
    */
    public String[] storeFile(MultipartFile file, String subDirectory) {
        // 1. 파일 검증
        validateFile(file);
        String originalFilename = file.getOriginalFilename();
        // 2. 파일 확장자 추출
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 3. UUID로 고유한 파일명 생성
        String storedFileName = UUID.randomUUID().toString() + extension;
        // 4. 날짜별 디렉토리 경로 설정 (boards/2024/06/20)
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String relativePath = subDirectory + File.separator + datePath + File.separator;
        // 5. 전체 경로 생성 (기본 경로 + 상대 경로)
        Path targetDir = this.uploadPath.resolve(relativePath).normalize();
        try {
		    // 6. 디렉토리 생성 (부모 디렉토리도 함께 생성)
            Files.createDirectories(targetDir);
            log.debug("디렉토리 생성 또는 이미 존재함: {}", targetDir);
            // 7. 파일 저장 (중복 시 덮어쓰기)
            Path targetLocation = targetDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 저장 성공: {} (원본명: {}, 크기: {} bytes, 경로: {})", storedFileName, originalFilename, file.getSize(), targetLocation);
            // 8. 저장된 파일 정보 반환 [파일명, 경로]
            return new String[] {storedFileName, relativePath.replace(File.separatorChar, '/')};
		} catch (IOException e) {
			log.error("파일 저장 실패 : {}", originalFilename,e);
            throw new RuntimeException("파일을 저장하는 중 오류가 발생했습니다.", e);
		}
    }
    /*
        파일 다운로드 - 저장된 파일을 Resource로 반환
            - 동작 과정
                - 전체 파일 경로 생성(기본 경로 + 상대 경로 + 파일명)
                - 파일을 URLResource로 변환
                - 파일 존재 여부 확인
                - Resource 반환(HTTP 응답으로 전달)
            - Resource란?
                - Spring의 파일 추상화 인터페이스
                - 파일 시스템, 클래스패스, URL 등 다양한 소스의 파일을 통일된 방식으로 다룸
                - 파일 다운로드 응답 생성
     */
    public Resource loadFileAsResource(String fileName, String storedFileName) {
        try {
            // 1. 전체 파일 경로 생성 및 정규화
            Path filePath = this.uploadPath.resolve(fileName).resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                log.info("파일 리소스 로드 성공: {} (경로: {})", fileName, filePath);
                return resource;
            } else {
                log.error("파일을 찾을 수 없거나 읽을 수 없음: {} (경로: {})", fileName, filePath);
                throw new RuntimeException("파일을 찾을 수 없습니다: " + storedFileName);
            }
        } catch (MalformedURLException e) {
            log.error("파일 다운로드 실패: {}", storedFileName,e);
            throw new RuntimeException("파일을 로드하는 중 오류가 발생했습니다: " + storedFileName, e);
        }
    }
    /*
        파일 삭제 - 물리적으로 파일을 디스크에서 삭제
            - 사용 시점
                - 게시글 삭제 시 (연관된 파일을 모두 삭제)
                - 파일 수정 시(기존 파일 삭제 후 새 파일 저장)
     */
    public void deleteFile(String fileName, String storedFileName) {
        try {
            Path file = this.uploadPath.resolve(storedFileName).resolve(fileName).normalize();
            // 파일 삭제(파일이 없어도 예외 발생하지 않음)
            Files.deleteIfExists(file);
            log.info("파일 삭제 완료 : {}", file);
        } catch (IOException e) {
            log.error("파일 삭제 실패 : {}", storedFileName,e);
        }
    }

    // 파일 확장자 추출 - 파일명에서 확장자를 추출하여 소문자로 변환
    public String getFileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex + 1).toLowerCase();
    }
}
