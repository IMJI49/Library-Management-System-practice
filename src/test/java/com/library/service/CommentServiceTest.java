package com.library.service;

import com.library.dto.comment.CommentCreateDTO;
import com.library.dto.comment.CommentDTO;
import com.library.entity.board.Board;
import com.library.entity.comment.Comment;
import com.library.entity.member.Member;
import com.library.repository.BoardRepository;
import com.library.repository.CommentRepository;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
    CommentService 단위 테스트
        - Mockito 사용한 단위 테스트
        - 실제 DB나 Spring Context 없이 Service 로직만 테스트
 */
@ExtendWith(MockitoExtension.class) // Mockito 사용을 위한 Junit 5 확장
public class CommentServiceTest {
    // Mock 객체 (실제 구현 대신 가짜 객체 주입)
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks    // 테스트 대상 : Mock 객체들이 주입된 실제 Service
    private CommentService commentService;

    /*
        댓글 생성
            - 정상케이스 테스트
                - 게시글과 작성자가 존재할 떄 댓글이 정상생성되는가?
                - 생성된 댓글의 내용이 올바른가?
                - Repository.save 메소드가 정확히 1번 호출되는가?
     */
    @Test
    void 댓글_정상작성() {
        Long boardId = 1L;
        String loginId = "test@test.test";
        String content = "테스트 댓글";
        CommentCreateDTO createDTO = CommentCreateDTO.builder().content(content).build();
        // Mock 게시글 엔티티 생성
        Board board = Board.builder().id(boardId).build();
        Member author = Member.builder().email(loginId).name("test").build();
        Comment comment = createCommentWithDates(
                1L, content, board, author, LocalDateTime.now(), LocalDateTime.now()
        );
        // Mock 동작 정의
        // boardRepository.findById가 호출 되면 board 반환
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(memberRepository.findByEmail(loginId)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        // When : 실제 테스트 대상 메소드 실행
        CommentDTO result = commentService.createComment(boardId, createDTO, loginId);
        // Then : 결과 검증
        assertThat(result.getContent()).isEqualTo(content);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    private Comment createCommentWithDates(Long id, String content, Board board, Member author, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Comment comment = Comment.builder()
                .id(id)
                .content(content)
                .board(board)
                .author(author)
                .build();
        // reflection으로 baseEntity의 날짜 필드 설정
        setBaseEntityFields(comment, createdAt, updatedAt);
        return comment;
    }

    /*
        리플렉션을 사용하여 날짜 필드 설정
            - 리플렉션은 런타임에 클래스 구조를 조작할 수 있는 강력한 도구
                - 런타임 시점에 클래스 정보를 조회하고 조작
                - 컴파일 타임이 아닌 실행 시점에 동적으로 작동
                - private, protected 필드나 메소드에도 접근 가능
     */
    private void setBaseEntityFields(Comment comment, LocalDateTime createdAt, LocalDateTime updatedAt) {
        try {
            // createdAt 필드 설정
            Field createdAtField = comment.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(comment,createdAt);
            Field updatedAtField = comment.getClass().getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(comment,updatedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("BaseEntity에 필드가 존재하지 않습니다." + e.getMessage(), e);
        } catch (Exception e){
            throw new RuntimeException("BaseEntity 필드 설정 오류 발생", e);
        }
    }
}
