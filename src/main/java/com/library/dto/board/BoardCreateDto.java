package com.library.dto.board;

import com.library.entity.board.BoardCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/*
    게시글 작성 요청 DTO (Data Transfer Object)
    Entity vs DTO
        - entity
            - 데이터베이스 테이블과 매핑
            - jpa가 관리하는 영속성 객체
        - dto
            - 사용자 입력값만 포함
            - title, content, category, files만 받음
            - 작성자 정보는 security context에서 가져옮
	    - 반드시 @Setter가 필요함
	사용 흐름
	    - 사용자 입력
	    - dto
	    - validation
	    - createBoard;
	    - entity 생성 dto-> entity 변환
	    - db 저장
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BoardCreateDto{
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 200,message = "제목은 200자를 초과할 수 없습니다.")
    private String title;
    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;
    @NotNull(message = "카테고리는 필수 선택 항목입니다.")
    private BoardCategory category;
    @Builder.Default
    private List<MultipartFile> files = new ArrayList<>();
}