package com.library.entity.board;

import com.library.entity.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

// 첨부 파일 정보 관리 board와 n:1관계
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "board_file")
public class BoardFile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id",nullable = false)
    /*
        jpa에서 양방향 관계를 맺을 때는 양쪽 모두 값을 설정해야 함
            - Board의 files 컬렉션에 파일 추가
            - BoardFile의 Board필드에 게시글 작성
            - 둘 중 하나만 설정하면 데이터 불일치가 발생할 수 있음.
        사용 방법
            - 이 메소드는 직접 호출하지 않고, Board.addFile 메소드 내에서 자동 호출
     */
    @Setter
    private Board board;
    @Column(nullable = false)
    private String originalFileName;
    @Column(nullable = false)
    private String storedFileName;  // 저장된 파일명 (uuid-파일명 중복 방지 위함)
    @Column(nullable = false,length = 500)
    private String filePath;
    @Column(nullable = false)
    private Long fileSize;
    @Column(nullable = false,length = 10)
    private String fileExtension;    // 파일 확장자 소문자
    @Column(nullable = false,length = 100)
    private String mimeType;    // mime 타입 pdf, jpg...
    @Column(nullable = false)
    @Builder.Default
    private Long downloadCount = 0L;
    // 다운로드 횟수 증가
    public void increaseDownloadCount() {
        this.downloadCount++;
    }
}