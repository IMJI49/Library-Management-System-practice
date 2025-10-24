package com.library.controller;

import com.library.entity.board.BoardFile;
import com.library.repository.BoardFileRepository;
import com.library.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/*
    파일 다운로드 Controller
        - 첨부파일 다운로드 처리
        - 다운로드 횟수 증가
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {
    private final FileStorageService fileStorageService;
    private final BoardFileRepository boardFileRepository;
    /*
        파일 다운로드
            - 파일 ID로 파일 정보 조회
            - 물리적 파일 로드
            - 다운로드 횟수 증가(더티 체킹)
            - 파일 다운로드 응답 반환
            - url files/download/id
     */
    @Transactional
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId){
        // 1. 파일 정보 조회
        BoardFile boardFile = boardFileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
        // 2. 물리적 파일 다운로드
        Resource resource = fileStorageService.loadFileAsResource(boardFile.getFilePath(), boardFile.getStoredFileName());
        // 3. 다운로드 횟수 증가
        boardFile.increaseDownloadCount();
        // @Transactional로 인해 메소드 종료시 더티 체킹으로 자동 업데이트
        // 4. 파일명 인코딩(한글 파일명 처리)
        String encodedFileName =
        URLEncoder.encode(boardFile.getOriginalFileName(), StandardCharsets.UTF_8);
//        try {
//            // 한글 깨짐 방지
//            encodedFileName = URLEncoder.encode(boardFile.getOriginalFileName(), StandardCharsets.UTF_8.toString());
//		} catch (UnsupportedEncodingException e) {
//            encodedFileName = boardFile.getOriginalFileName();
//        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName +"\"")
                .body(resource);
    }
}
