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

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;

    @GetMapping("")
    public String list(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size, Model model) {
        // Service를 통해 게시글 목록 조회
        Page<BoardListDto> lists = boardService.getBoardList(page, size);
        model.addAttribute("lists",lists);
        return "board/list";
    }
}
