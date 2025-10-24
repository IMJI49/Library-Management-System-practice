package com.library;

import com.library.entity.board.Board;
import com.library.entity.comment.Comment;
import com.library.entity.comment.CommentStatus;
import com.library.entity.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/*
    전체 Spring Context를 로드하여 테스트
        - 어플리케이션이 정상적으로 시작되는지
        - 모든 Bean이 정상적으로 생성되는지 검증
 */
@SpringBootTest
class LibraryManagementSystemApplicationTests {

    @Test
    void contextLoads() {
    }

    /*
        엔티티 검증
            - 순수 자바 POJO 테스트
            - DB 접근 없이 메모리상에서만 객체 검증
            - 테이블 없이 실행 가능
     */
    @Test
    void commentEntityTest() {
        // given 테스트 데이터 준비
        Board board = Board.builder()
                .title("title")
                .content("content")
                .build();
        Member author = Member.builder()
                .name("author")
                .password("password")
                .email("test1@test.tset")
                .build();
        Comment comment = Comment.builder()
                .content("content")
                .author(author)
                .board(board)
                .status(CommentStatus.ACTIVE)
                .build();
        // 검증
        assertThat(comment.getContent()).isEqualTo("content");
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getBoard()).isEqualTo(board);
        assertThat(comment.getStatus()).isEqualTo(CommentStatus.ACTIVE);
    }
}
