package com.library.repository;

import com.library.entity.board.BoardFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
    Spring Data JPA Query Method
        - 메소드 이름을 분석해서 자동으로 SQL 쿼리 생성하는 기능
 */
public interface BoardFileRepository extends JpaRepository<BoardFile, Long> {
    List<BoardFile> findByBoardIdOrderByCreatedAtDesc(Long boardId);
}
