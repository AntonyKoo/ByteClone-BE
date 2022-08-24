package com.whochucompany.byteclone.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentListDto {
    private Long id;
    private String username;
    private String comment;
    private LocalDateTime createAt;
}
