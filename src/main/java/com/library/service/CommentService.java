package com.library.service;

import com.library.dto.comment.CommentCreateDTO;
import com.library.dto.comment.CommentDTO;
import com.library.dto.comment.CommentUpdateDto;
import com.library.entity.board.Board;
import com.library.entity.comment.Comment;
import com.library.entity.comment.CommentStatus;
import com.library.entity.member.Member;
import com.library.repository.BoardRepository;
import com.library.repository.CommentRepository;
import com.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
    댓글 서비스 - 댓글 관련 비지니르 로직을 처리함
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    // 조회
    public List<CommentDTO> getComments(Long boardId) {
        log.info("게시글 {}의 목록 조회", boardId);
        return commentRepository.findByBoardIdAndStatus(boardId, CommentStatus.ACTIVE).stream().map(CommentDTO::from).toList();
    }

    // 작성
    @Transactional
    public CommentDTO createComment(Long boardId, CommentCreateDTO createDTO, String loginId) {
        log.info("게시글 {}의 댓글 작성 - 작성자 : {}", boardId, loginId);
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없스니다."));
        Member author = memberRepository.findByEmail(loginId).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Comment comment = Comment.builder()
                .board(board)
                .author(author)
                .content(createDTO.getContent())
                .build();
        Comment saved = commentRepository.save(comment);
        log.info("댓글 작성 완료 - 댓글 ID : {}", saved.getId());
        return CommentDTO.from(saved);
    }
    // 수정
    @Transactional
    public CommentDTO updateComment(Long commentId, CommentUpdateDto  updateDTO, String loginId) {
        log.info("댓글 {} 수정 - 수정자 : {}", commentId,  loginId);
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!comment.getAuthor().getEmail().equals(loginId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }
        if (comment.getStatus().equals(CommentStatus.DELETED)){
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }
        comment.update(updateDTO.getContent());
        log.info("댓글 수정 완료 - 댓글 ID : {}", commentId);
        Comment saved = commentRepository.save(comment);
        return CommentDTO.from(saved);
    }
    @Transactional
    public void deleteComment(Long commentId, String loginId) {
        log.info("댓글 {} 삭제 - 삭제자 : {}", commentId,  loginId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));
        if (!comment.getAuthor().getEmail().equals(loginId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }
        comment.delete();
        log.info("댓글 삭제 완료 - 댓글 ID : {}", commentId);
    }
    public Long countComments(Long boardId) {
        return commentRepository.countByBoardIdAndStatus(boardId, CommentStatus.ACTIVE);
    }
}
