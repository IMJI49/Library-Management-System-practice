package com.library.repository;

import com.library.entity.board.Board;
import com.library.entity.comment.Comment;
import com.library.entity.comment.CommentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface CommentRepository extends CrudRepository<Comment, Long> {
    /*
        특정 게시글 활성화 상태 댓글 목록 조회
            - Fetch Join으로 작성자 정보를 함께 조회 하여 N+1ㅡ문제 방지
            - 오름차순
     */
    @Query("select c FROM Comment c join fetch c.author where c.board.id = :boardId and c.status = :status " +
            "order by c.createdAt asc ")
    List<Comment> findByBoardIdAndStatus(Long boardId, CommentStatus status);
    /*
        특정 게시글 활성 상태 댓글 갯수 조회
            - spring data jpa의 쿼리 메소드 네이밍 규칙 사용
                - count
            - 자동으로 sql의 count 쿼리 생성
     */
    Long countByBoardIdAndStatus(Long boardId, CommentStatus status);
    long countByBoardAndStatus(Board board, CommentStatus status);
}
