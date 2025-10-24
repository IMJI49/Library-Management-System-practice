package com.library.dto.board;

import com.library.entity.board.BoardFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
    게시글 첨부파일 DTO
        - 게시글 상세 조회시 첨부파일 정보를 클라이언트에 전달
        - 엔티티에서 필요한 정보만 선별하여 노출
        - 파일 다운로드 링크 생성에 필요한 정보 제공
        - 읽기 전용
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class BoardFileDto {
    private Long id;    // 다운로드 링크 생성시
    private String originalFileName;
    private String storedFileName;
    private Long boardId;
    private String filePath;
    private Long fileSize;
    private String fileExtension;
    private Long downloadCount;
    // 사람이 읽기 쉬운 파일 크기 문자열 리턴하는 메소드
    public String getFormatedFileSize() {
        if(fileSize < 1024){
            return fileSize + "B";
        } else if(fileSize < 1024*1024){
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if(fileSize < 1024*1024*1024){
            return String.format("%.1f MB", fileSize / 1024.0 / 1024.0);
        } else {
            return String.format("%.1f GB", fileSize / 1024.0 / 1024.0 / 1024.0);
        }
    }
    /*
        정적 팩토리 메소드 - entity를 dto로 변환 해주는 메소드
            - static 메소드로 객체 생성 패턴
            - 생성자 대신 의미 있는 이름으로 객체 생성
            - 캡슐화
            - 장점
                - 명확히 표현
                - 생성 로직 중앙화
                - 필요한 필드만 선택적 복사
                - stream과 조합
     */
    public static BoardFileDto from(BoardFile boardFile) {
        return BoardFileDto.builder()
                .id(boardFile.getId())
                .originalFileName(boardFile.getOriginalFileName())
                .storedFileName(boardFile.getStoredFileName())
                .filePath(boardFile.getFilePath())
                .fileSize(boardFile.getFileSize())
                .fileExtension(boardFile.getFileExtension())
                .downloadCount(boardFile.getDownloadCount())
                .build();
    }
    public String getFileType() {
        String ext = fileExtension.toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif" -> "image";
            case "pdf" -> "pdf";
            case "doc", "docx", "hwp" -> "document";
            case "xls", "xlsx" -> "excel";
            case "ppt", "pptx" -> "powerpoint";
            case "zip", "rar", "gz", "bz2" -> "archive";
            case "txt" -> "text";
            default -> "default";
        };
    }
    public String getFileIconClass() {
        String type = getFileType();
        return switch (type) {
            case "image" -> "fa-file-image";
            case "pdf" -> "fa-file-pdf";
            case "document" -> "fa-file-document";
            case "excel" -> "fa-file-excel";
            case "powerpoint" -> "fa-file-powerpoint";
            case "archive" -> "fa-file-archive";
            case "text" -> "fa-file-alt";
            default -> "fa-file";
        };
    }
}