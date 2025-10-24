package com.library.controller;

import com.library.dto.comment.CommentCreateDTO;
import com.library.dto.comment.CommentDTO;
import com.library.dto.comment.CommentUpdateDto;
import com.library.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
@Slf4j
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/boards/{boardId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByBoardId(@PathVariable Long boardId) {
        log.info("댓글 목록 조회 요청 - 게시글 ID {}", boardId);
        List<CommentDTO> comments = commentService.getComments(boardId);
        log.info("댓글 목록 조회 완료 - 댓글 수 : {}", comments.size());
        return ResponseEntity.ok(comments);
    }

    /*
        댓글 작성 API
            - post
            - 특정 게시글에 새 댓글 작성
     */
    @PostMapping("/boards/{boardId}")
    public ResponseEntity<CommentDTO> createComment(@PathVariable Long boardId, @Valid @RequestBody CommentCreateDTO commentCreateDTO, @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        log.info("댓글 작성 요청 - 게시글 ID : {}, 작성자 : {}", boardId, userEmail);
        CommentDTO comment = commentService.createComment(boardId, commentCreateDTO, userEmail);
        log.info("댓글 작성 완료 - 댓글 ID : {}", comment.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentUpdateDto updateDto, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("댓글 수정 요청 - 댓글 ID : {}, 수정자 : {}", commentId, username);
        CommentDTO comment = commentService.updateComment(commentId, updateDto, username);
        log.info("댓글 수정 완료 - 댓글 ID : {}", commentId);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("댓글 삭제 요청 - 댓글 ID : {}, 삭제자 : {}", commentId, username);
        commentService.deleteComment(commentId, username);
        log.info("댓글 삭제 완료 - 댓글 ID : {}", commentId);
        return ResponseEntity.noContent().build();
    }
}
