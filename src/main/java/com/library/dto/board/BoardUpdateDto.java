package com.library.dto.board;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.library.entity.board.BoardCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
    BoardCreateDTO와 차이점
        - 기존 파일 삭제를 위한 deleteFileIds 필드 추가
        - 게시판 ID는 URL 파라미터로 전달되므로 DTO에 포함하지 않음
    사용 흐름
        - 사용자가 수정 폼에서 입력
        - BoardUpdateDTO (데이터 바인팅)
        - Validation 검증 (@Valid)
        - BoardService.updateBoard()
        - Board Entity의 update() 메소드 호출 (더티 체킹)
        - DB 자동 update
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BoardUpdateDto {
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 200, message = "제목은 200자를 초과 할 수 없습니다.")
    private String title;
    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;
    @NotNull(message = "카테고리는 필수 선택 항목입니다.")
    private BoardCategory category;
    // 새로 추가 할 파일 리스트
    @Builder.Default
    private List<MultipartFile> files = new ArrayList<>();
    // 삭제 할 기존 파일 리스트
    @Builder.Default
    private List<Long> deleteFileIds = new ArrayList<>();
}