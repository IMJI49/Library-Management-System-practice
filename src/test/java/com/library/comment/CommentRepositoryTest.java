package com.library.comment;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.library.entity.board.Board;
import com.library.entity.comment.Comment;
import com.library.entity.comment.CommentStatus;
import com.library.entity.member.Member;
import com.library.repository.BoardRepository;
import com.library.repository.CommentRepository;
import com.library.repository.MemberRepository;

/*
    CommentRepository 테스트 클래스
        - 목적 : 댓글 리포짓토리 데이터베이스
        - 테스트 항목
            - repository안에 있는 두 메소드
        - @Auto...
            - application.yml에 설정된 DB를 무시하고 테스트용 embedded database로 교체
            - 실제 DB대신 H@ In-memory DB를 사용하여 테스트 격리
            - 각 테스트 메소드 실행 후 트랜잭션 자동 롤백으로 테스트 간 데이터 독립성 보장
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private MemberRepository memberRepository;
    private Member testAuthor;
    private Comment testComment;
    private Board testBoard;
    @BeforeEach
    void setup(){
        // 1단계 : 테스트용 회원 생성
        testAuthor = Member.builder()
                .name("Test Author")
                .password("password")
                .email("test@test.test")
                .phone("010-1234-5678")
                .address("address")
                .build();
        memberRepository.save(testAuthor);
        testBoard = Board.builder()
                .title("Test Board")
                .content("This is a test board")
                .author(testAuthor)
                .build();
        boardRepository.save(testBoard);
        testComment = Comment.builder()
                .author(testAuthor)
                .content("This is a test comment")
                .board(testBoard)
                .build();
        commentRepository.save(testComment);
    }
    /*
        특정 게시물의 활성 상태 댓글만 정확히 조회되는지 검증
            - 검증 사항
                - ACTIVE 상태 댓글만 조회 되는가?
                - DELETED 상태의 댓글은 조회되지 않는가? 등
        given-when-then
     */
    @Test
    public void findByBoardIdAndStatus(){
        Comment activeComment = Comment.builder()
                                .content("첫번째 댓글")
                                .board(testBoard)
                                .author(testAuthor)
                                .build();
        commentRepository.save(activeComment);
        // 삭제된 상태 댓글 생성
        Comment deletedComment = Comment.builder()
                .content("두번째 댓글")
                .board(testBoard)
                .author(testAuthor)
                .status(CommentStatus.DELETED)
                .build();
        commentRepository.save(deletedComment);
        List<Comment> activeComments = commentRepository.findByBoardIdAndStatus(testBoard.getId(), CommentStatus.ACTIVE);
        // Then : 결과 검증 - 활성 댓글 2개만 조회 내용이 올바른지 확인
        assertThat(activeComments.size()).isEqualTo(2);
        assertThat(activeComments.get(0).getContent()).isEqualTo(testComment.getContent());
        assertThat(activeComments.get(0).getStatus()).isEqualTo(CommentStatus.ACTIVE);

    }
    @Test
    public void countByBoardIdAndStatus(){
        for (int i = 0; i < 3; i++) {
            Comment comment =Comment.builder()
                    .content("댓글 "+i)
                    .board(testBoard)
                    .author(testAuthor)
                    .build();
            commentRepository.save(comment);
        }
        @SuppressWarnings("unused")
		Comment deletedComment = Comment.builder()
                .content("두번째 댓글")
                .board(testBoard)
                .author(testAuthor)
                .status(CommentStatus.DELETED)
                .build();
        Long commentNum = commentRepository.countByBoardIdAndStatus(testBoard.getId(), CommentStatus.ACTIVE);
        assertThat(commentNum).isEqualTo(4);
        assertThat(commentRepository.countByBoardIdAndStatus(testBoard.getId(), CommentStatus.DELETED)).isEqualTo(1);
    }
    @Test
    public void countByBoardAndStatus(){
        long deletedCommentNum = commentRepository.countByBoardAndStatus(testBoard, CommentStatus.DELETED);
        assertThat(deletedCommentNum).isEqualTo(0);
    }
}
