package com.library.service;

import com.library.dto.board.BoardListDto;
import com.library.entity.board.BoardStatus;
import com.library.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/*
    게시글 Service
        - 게시글 관련 비지니스 로직을 처리함
        - 트랜잭션 관리 및 Entity와 DTO 간 변환을 담당함
        - N+1 문제 해결
            - 게시글 목록 조회 시 작성자 정보(author)도 함께 조회
            - Fetch Join을 사용하는 repository메소드 활용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;
    /*
        게시글 목록 조회(페이징)
            - ACTIVE 상태의 게시글만 조회하며 최신순으로 정렬함
            - Entity를 DTO로 변환하여 반환
            - N+1 문제 해결
                - findByStatusWithAuthor() 메소드 사용
                - Board와 Member를 Join으로 한 번에 조회
                - BoardListDto 변환시 author.getName() 호출해도 추가 쿼리 없음
        @param page 조회할 페이지 변호(0부터 시작)
        @param size 페이지당 게시글 수
        @return 페이징 된 게시글 목록(BoardListDto)
     */
    public Page<BoardListDto> getBoardList(int page, int size) {
        /*
            ACTIVE상태의 게시글 조회
            Entity -> DTO
            page.map() : page 내부의 각 board entity를 BoardListDto로 변환
            BoardListDto::from - 메소드 레퍼런스 (Board -> BoardListDto.from(board))
         */
    	return boardRepository.findByStatusWithAuthor(BoardStatus.ACTIVE,PageRequest.of(page, size, Sort.by("createdAt").descending())).map(BoardListDto::from);
    }
}
