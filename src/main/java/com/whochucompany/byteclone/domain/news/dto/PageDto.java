package com.whochucompany.byteclone.domain.news.dto;

import com.whochucompany.byteclone.controller.ResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private Long TotalElement;
    private Long TotalPages;
    private int NowPage;
    private int NowContent;
    private List<NewsResponseDto> content;

}
