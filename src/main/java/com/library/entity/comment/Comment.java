package com.library.entity.comment;

import com.library.entity.board.Board;
import com.library.entity.entity.BaseEntity;
import com.library.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false, length = 100)
    private String content;
    @Builder.Default
    private Long likeCount = 0L;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.ACTIVE;
    // 연관 관계 메소드
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;
    public void update(String content) {
        this.content = content;
    }
    public void delete(){
        this.status = CommentStatus.DELETED;
    }

}