package com.library.dto.board;

import com.library.entity.board.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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
public class BoardDto implements Serializable {
    LocalDateTime createdAt;
    Long id;
    String title;
    BoardCategory category;
}