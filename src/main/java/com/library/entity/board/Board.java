package com.library.entity.board;

import com.library.entity.entity.BaseEntity;
import com.library.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;

/*
    게시글 Entity
        - 게시판의 게시글 정보를 관리함
        - BaseEntity를 상속 받아 생성 일시 / 수정 일시가 자동 관리됨
        - 작성자(Member)와 다대일 관계(N:1)
     연관 관계 로딩 전략
        - author 필디는 지연 로딩(lazy) 사용
        - N + 1 문제를 해결을 위해 Fetch Join 권장
        - 조회 시 BoardRepository의 Fetch Join 메소드 사용 필요
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // jpa 스펙상 기본생상자 필요. 외부에서 직접 생성 방지
@Getter
@Entity
@Table(name = "board")
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false,length = 200)
    private String title;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    /*
        게시글 : 작성자 N:1
            - 연관 관계 설정
                - 관계 다대일
                - 외래키 : author_id
                - 필수 : nullable = false
            - 지연 로딩
                - 성능 최적화함
                - 장점 : Board 조회 시 Member를 즉시 조회하지 않음
                - 효과 : author 필드를 사용하지 않는 경우 불필요한 Join쿼리 방지
                - 동작 : board.getAuthor().getName() 호출 시점에 Member 조회
            - N + 1 문제와 해결책
                - N + 1 문제란?
                    - 게시글 목록 조회 :1번의 쿼리
                    - 각 게시글의 작성자 조회 : N번의 추가 쿼리
                    - 결과 : 게시글이 100개면 총 101번의 쿼리 실행
                - 해결책
                    - Fetch Join 사용
                        - BoardRepository에서 Fetch Join으로 한 번에 조회
                        - 1번의 쿼리로 Board + Member 함께 조회
                - 프록시 객체
                    - 실제 Member 객체가 아닌 Hibernate 프록시 객체
                    - author의 메소드 호출 시점에 실제 DB 조회
                    - 트랜잭션 범위 밖에서 접근시 LazyInitializationException 발생 가능
                        - 해결 : @Transactional 내에서 사용하거나 Fetch Join 활용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id",  nullable = false)
    private Member author;
    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;
    @Column(nullable = false)
    @Builder.Default
    private Long likeCount = 0L;
    /*
        개시글 상태
            - EnumType.STRING을 사용하여 문자열로 저장
            - 값 기반 저장
            - 가독성 : DB에서 직접 확인 가능
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    @Builder.Default
    private BoardStatus status = BoardStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    @Builder.Default
    private BoardCategory category = BoardCategory.FREE;
    /*
            비지니스 메소드
                - 조회수 증가
                    - 게시글 상세보기 시 호출됨
                    - 트랜잭션 내에서 호출되어야 변경사항이 DB에 반영됨
     */
    public void increaseViewCount() {
        this.viewCount++;
    }
    public void increaseLikeCount() {
        this.likeCount++;
    }
    /*
        게시글 수정
            - 게시글의 제목, 본문, 카테고리를 수정함
     */
    public void update(String title, String content, BoardCategory category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
    /*
        게시글 삭제
            - 소프트 삭제 : 실제 데이터를 삭제하지 않고 상태만 DELETED로 변경함
                - 장점 : 데이터 복구 가능, 감사추적(Audit Trail)  유지, 통계 데이터 보존, 외래키 제약 조건 유지
     */
    public void delete() {
        this.status = BoardStatus.DELETED;
    }
}