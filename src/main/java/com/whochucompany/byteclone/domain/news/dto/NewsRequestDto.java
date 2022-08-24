package com.whochucompany.byteclone.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsRequestDto {
    private String title;
    private String content;
    private MultipartFile image;
    private String view; // 읽기 권한 설정하는 카테고리 as enum
    private String category; // 주제별 구분 카테고리 as enum

}
