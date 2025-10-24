package com.library.repository;

import com.library.entity.board.Board;
import com.library.entity.board.BoardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/*
    게시글 Repository
        - Board Entity의 데이터베이스 접근을 담당
        - Spring Data JPA의 메소드 네이밍 규칙을 활용하여 쿼리를 자동 생성삼
            - 메소드 네이밍 규칙
                - findBy : select 쿼리 생성
 */
public interface BoardRepository extends JpaRepository<Board, Long> {
    /*
        게시글 상태별 목록 조회(페이징, 작성자 정보 포함)
            - N + 1 문제를 방지하기 위해 Fetch Join을 사용함
            - Fetch Join은 메소드 네이밍으로 표현할 수 없어 @Query로 명시적 작성이 필요함
            - 명시적 작성 필요
            - countQuery분리 이유
                - 전체 개수 조회 시 Join 불필요(성능 최적화)
                - Board 테이블만 Count 하면 충분
                - order by 도 불필요(개수만 세면 됨)
        파라미터 바인딩 :
            - :status <= 메소드의 BoardStatus status 파라미터와 자동 매칭
        @param status : 조회할 게시글 상태
        @param pageable : 페이징 정보
     */
	@Query(
            // 데이터 조회 쿼리
            value = "Select b " +
                    "From Board b " +
                    "JOIN FETCH b.author " +   // Member도 함께 로드(N+1 방지)
                    "Where b.status = :status " +
                    "ORDER By b.createdAt DESC",
            // 갯수 조회 쿼리(페이징용)
            countQuery = "select count(b) " +   // 개수 세기
                         "from Board b " +
                    "where b.status = :status"
    )
    Page<Board> findByStatusWithAuthor(BoardStatus status, Pageable pageable);
    /*
        게시글 단건 조회 Id+상태, 작성자 정보 포함
            - N+1 문제를 방지하기 위해 FetchJoin을 사용함
            - Active 상태의 게시글만 조회(삭제된 글은 조회가 불가)
     */
    @Query("select b from Board b join fetch b.author where b.id = :id and b.status = :status")
    Optional<Board> findByIdAndStatusWithAuthor(Long id, BoardStatus status);
}
