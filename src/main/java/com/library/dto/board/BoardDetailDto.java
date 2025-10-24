package com.library.dto.board;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.library.entity.board.Board;
import com.library.entity.board.BoardCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
    게시글 상세 조회용 DTO
        - 게시글의 전체 내용을 포함함
            - 포함 정보
                - 게시글 상세 정보 : ID, 제목, 본문, 카테고리, 작성일시, 수정일시
                - 통계정보 : 조회수, 좋아요, 댓글수
                - 작성자 정보 : 이름, 이메일
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardDetailDto {

    private Long id;
    private String title;
    private String content;
    private String authorEmail;
    private String authorName;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private BoardCategory category;
    @Builder.Default
    private List<BoardFileDto> files = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /*
        Board Entity를 BoardDetailDto로 변환하는 정적 메소드
            - Board Entity와 연관된 Member Entity의 정보를 함께 추출
     */
    public static BoardDetailDto from(Board board) {
        return BoardDetailDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorEmail(board.getAuthor().getEmail())
                .authorName(board.getAuthor().getName())
                .viewCount(board.getViewCount())
                .likeCount(board.getLikeCount())
                .commentCount(0L)
                .category(board.getCategory())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .files(board.getFiles().stream().map(BoardFileDto::from).toList())
                .build();
    }
}