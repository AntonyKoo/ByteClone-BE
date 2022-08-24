package com.whochucompany.byteclone.domain.news.dto;


import com.whochucompany.byteclone.domain.comment.dto.CommentListDto;
import com.whochucompany.byteclone.domain.news.enums.Category;
import com.whochucompany.byteclone.domain.news.enums.View;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewsDetailResponseDto {
    private Long newsId;
    private String title;
    private String username;  // 작성자
    private String image;
    private View view;  // 뉴스 기사 읽기 권한
    private Category category;  // 뉴스 기사 카테고리
    private LocalDateTime createdAt;
    private String content;
    private List<CommentListDto> commentList;
}
