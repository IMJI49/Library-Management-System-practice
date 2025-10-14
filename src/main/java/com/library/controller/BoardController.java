package com.library.controller;

import com.library.dto.board.BoardListDto;
import com.library.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
/*
    게시글 Controller
        - 게시글 관련 HTTP요청을 처리하고 뷰를 반환함
        - URL 맵핑 : /board
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;
    /*
        게시글 목록 페이지
            - ACTIVE 상태의 게시글 목록을 페이징하여 조회함
            - View와 로직 분리 원칙에 따라 페이징 계산은 Controller에서 처리함
            - 작성일 최신순 정렬로 인해 ID
        1-based 페이징 시스템(URL : page=1부터 시작)
            - URL : get /board?page=?&size=?
        페이징 그룹 개념
            - 한 번에 10개의 페이지 번호만
        -
     */
    @GetMapping("")
    public String list(@RequestParam(defaultValue = "1") int page,@RequestParam(defaultValue = "10") int size, Model model) {
        // Service를 통해 게시글 목록 조회
        Page<BoardListDto> boards = boardService.getBoardList(page-1, size);
//      전체 페이지 수
        int totalPages = boards.getTotalPages();
        // 한 그룹에 표시할 페이지 버튼 개수
        int pageGroupSize = 10; // 한 그룹에 표시할 페이지 버튼 개수
        int currentGroup = (page - 1)/pageGroupSize;
        int startPage = currentGroup * pageGroupSize + 1;
        // 그룹의 종료 페이지 번호
        int endPage = Math.min(totalPages, startPage + pageGroupSize - 1);
        boolean hasPrevGroup = startPage > 1;
        // 이전 그룹의 마지막 페이지
        int prevGroupPage = startPage - 1;
        boolean hasNextGroup = endPage < totalPages;
        // 다음 그룹의 첫 페이지
        int nextGroupPage = endPage + 1;

        model.addAttribute("boards",boards);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", boards.getTotalElements());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("hasPrevGroup", hasPrevGroup);
        model.addAttribute("hasNextGroup", hasNextGroup);
        model.addAttribute("prevGroupPage", prevGroupPage); // 이전 그룹으로 이동 시 페이지 번호
        model.addAttribute("nextGroupPage", nextGroupPage);
        return "board/list";
    }
}
