package com.library.dto.board;

import java.time.LocalDateTime;

import com.library.entity.board.Board;
import com.library.entity.board.BoardCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 목록 조회용 DTO
 * - 전체 게시글 내용은 포함하지 않아 네트워크 전송량을 최저고하함
 * - 포함 정보
 * - 게시글 기본 정보 : ID, 제목, 카테고리, 작성일시
 * - 통계정보 : 조회수, 좋아요 수, 댓글 수
 * - 작성자 정보 : Member 이름
 *
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardListDto{
    private LocalDateTime createdAt;
    private Long id;
    private String title;
    private BoardCategory category;
    private String authorName;
    private Long viewCount;
    private Long LikeCount;
    private Long CommentCount;
    /*
        board Entity를 BoardListDto로 변환하는 정적 메소드
            - Board Entity와 연관된 Member Entity의 정보를 함께 추출함
     */
    public static BoardListDto from(Board board) {
        return BoardListDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .category(board.getCategory())
                .authorName(board.getAuthor().getName())
                .viewCount(board.getViewCount())
                .LikeCount(board.getLikeCount())
                .CommentCount(0L)
                .createdAt(board.getCreatedAt())
                .build();
    }
}