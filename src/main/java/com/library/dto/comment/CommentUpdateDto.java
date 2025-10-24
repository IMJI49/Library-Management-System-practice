package com.library.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CommentUpdateDto {
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 1000, message = "댓글은 최대 1000자까지 입력 가능합니다.")
    private String content;
}