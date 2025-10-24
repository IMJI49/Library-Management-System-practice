package com.library.controller;

import com.library.dto.board.BoardCreateDto;
import com.library.dto.board.BoardDetailDto;
import com.library.dto.board.BoardListDto;
import com.library.dto.board.BoardUpdateDto;
import com.library.entity.board.BoardCategory;
import com.library.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

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
    public String list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        // Service를 통해 게시글 목록 조회
        Page<BoardListDto> boards = boardService.getBoardList(page - 1, size);
//      전체 페이지 수
        int totalPages = boards.getTotalPages();
        // 한 그룹에 표시할 페이지 버튼 개수
        int pageGroupSize = 10; // 한 그룹에 표시할 페이지 버튼 개수
        int currentGroup = (page - 1) / pageGroupSize;
        int startPage = currentGroup * pageGroupSize + 1;
        // 그룹의 종료 페이지 번호
        int endPage = Math.min(totalPages, startPage + pageGroupSize - 1);
        boolean hasPrevGroup = startPage > 1;
        // 이전 그룹의 마지막 페이지
        int prevGroupPage = startPage - 1;
        boolean hasNextGroup = endPage < totalPages;
        // 다음 그룹의 첫 페이지
        int nextGroupPage = endPage + 1;

        model.addAttribute("boards", boards);
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

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, Model model) {
        // Service를 통해 게시글 상세 정보 조회
        BoardDetailDto board = boardService.getBoard(id);
        model.addAttribute("board", board);
        model.addAttribute("currentPage", page);
        return "board/detail";
    }

    /*
        게시글 작성 폼 페이지
            - 새 게시글 작성을 위한 폼을 표시함
            - 카테고리 목록을 함께 전달하여 선택하도록 함
            - get board/new
     */
    @GetMapping("/new")
    public String createFrom(Model model) {
        // 빈 DOO객체 생성(Thymeleaf Form 바인딩용)
        model.addAttribute("board", new BoardCreateDto());
        model.addAttribute("categories", BoardCategory.values());
        model.addAttribute("isEditMode", false);
        return "board/form";
    }

    @PostMapping("")
    public String createBoard(@Valid @ModelAttribute BoardCreateDto dto, BindingResult bindingResult, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        // 검증 실패 처리
        if (bindingResult.hasErrors()) {
            model.addAttribute("board", dto);
            model.addAttribute("categories", BoardCategory.values());
            model.addAttribute("isEditMode", false);
            return "board/form";    // 폼으로 돌아감
        }
        try {
            String username = principal.getName();
            Long boardId = boardService.createBoard(dto, username);
            redirectAttributes.addFlashAttribute("success", "게시글이 작성되었습니다.");
            return "redirect:/boards/" + boardId;
        } catch (Exception e) {
            // 예외 발생 처리
            // 게시글 생성 중 예외 발생
            model.addAttribute("error", "게시글 작성 중 오류가 발생했습니다." + e.getMessage());
            model.addAttribute("errorType", "system_error");
            // 카테고리 목록 다시 추가 (폼 재표시용)
            model.addAttribute("board", dto);
            model.addAttribute("categories", BoardCategory.values());
            model.addAttribute("isEditMode", false);
            return "board/form";
        }
    }

    /*
        게시글 삭제 처리
            - 코드 흐름
                1) principal에서 현재 로그인한 사용자 이메일 획득
                2) Service의 deleteBoard 호출ㄹ
                3) 성공 메세지 flash에 추가
                4) 게시글 목록으로 리다이렉트
     */
    @DeleteMapping("/delete/{id}")
    public String deleteBoard(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            boardService.deleteBoard(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "게시글이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/boards";
    }

    /*
        게시글 수정 폼 페이지
            - 기존 게시글 정보를 조회하여 폼에 표시함
            - 작성자 본인만 접근 가능
            - URL : get /edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, Principal principal) {
        try {
            String username = principal.getName();
            BoardDetailDto board = boardService.getBoardForEdit(id, username);
            // BoardUpDateDTO로 변환하여 폼에 바인딩
            BoardUpdateDto updateDto = BoardUpdateDto.builder()
                    .title(board.getTitle())
                    .content(board.getContent())
                    .category(board.getCategory())
                    .build();
            model.addAttribute("board", updateDto);
            model.addAttribute("categories", BoardCategory.values());
            model.addAttribute("boardId", id);
            model.addAttribute("existingFiles", board.getFiles());
            model.addAttribute("isEditMode", true);
            return "board/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/boards/" + id;
        }
    }
    /*
        게시글 수정 처리
            - 제목, 내용, 카테고리 수정
            - 기존 파일 삭제 및 새 파일 추가
            - 작성자 본인만 수정 가능
            - URI : put "
     */
    @PutMapping("/edit/{id}")
    public String editBoard(@PathVariable Long id, @ModelAttribute("board") BoardUpdateDto updateDto, BindingResult bindingResult, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        // 검증 실패 처리
        if (bindingResult.hasErrors()) {
            model.addAttribute("board", updateDto);
            model.addAttribute("categories", BoardCategory.values());
            model.addAttribute("boardId", id);
            model.addAttribute("isEditMode", true);
            try {
                BoardDetailDto board = boardService.getBoardForEdit(id, principal.getName());
                model.addAttribute("existingFiles", board.getFiles());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
            return "board/form";
        }
        try {
            // 현재 로그인 한 사용자 이름
            String userEmail = principal.getName();
            boardService.updateBoard(id,userEmail, updateDto);
            redirectAttributes.addFlashAttribute("success", "게시글이 수정되었습니다.");
            return "redirect:/boards/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            model.addAttribute("errorType", "system_error");
            model.addAttribute("boardId", id);
            model.addAttribute("categories", BoardCategory.values());
            model.addAttribute("board", updateDto);
            model.addAttribute("isEditMode", true);
            // 기존 파일 목록 다시 조회
            try {
                BoardDetailDto board = boardService.getBoardForEdit(id, principal.getName());
                model.addAttribute("existingFiles", board.getFiles());
            } catch (Exception ex) {
                // 파일 목록 실패해도 폼은 표시
                throw new RuntimeException(ex);
            }
            return "board/form";
        }
    }
}
