package com.whochucompany.byteclone.controller;

import com.whochucompany.byteclone.domain.news.News;
import com.whochucompany.byteclone.domain.news.dto.NewsRequestDto;
import com.whochucompany.byteclone.domain.news.dto.NewsResponseDto;
import com.whochucompany.byteclone.domain.news.enums.Category;
import com.whochucompany.byteclone.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/news")
public class NewsController {

    private final NewsService newsService;

    // 뉴스기사 작성
    @PostMapping
    public ResponseDto<?> createNews(@ModelAttribute NewsRequestDto requestDto, HttpServletRequest request) throws IOException {
        System.out.println("request.getHeader(\"Authorization\") = " + request.getHeader("Authorization"));

        return newsService.createNews(requestDto, request);
    }

    // 뉴스기사 수정
    @PutMapping(value = "/{newsId}")
    public ResponseDto<?> updateNews(@PathVariable Long newsId, @ModelAttribute NewsRequestDto requestDto, HttpServletRequest request) throws IOException {
        return  newsService.updateNews(newsId, requestDto, request);
    }

    // 뉴스기사 전체 조회 == 메인 페이지
    @GetMapping
    public ResponseEntity<?> readAllNewsList(
            @PageableDefault(size = 50) Pageable pageable
    ) {
        Page<NewsResponseDto> news = newsService.findAll(pageable);
        return new ResponseEntity<>(news, HttpStatus.OK);
    }

    // Category 별 전체 조회
    @GetMapping(value = "/{category}")
    public ResponseEntity<?> readAllNewsListByCategory(
            @PathVariable("category") Category category,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        System.out.println("category = " + category);

        Page<NewsResponseDto> news;
        news = newsService.findAllByCategory(category, pageable);

        return new ResponseEntity<>(news, HttpStatus.OK);
    }

    @GetMapping(value = "/detail/{newsId}")  //상세조회
    public ResponseEntity<?> readDetailNews(@PathVariable Long newsId){
        return newsService.readDetailNews(newsId);
    }

    @DeleteMapping(value = "/{newsId}")
    public ResponseDto<?> deleteNews(@PathVariable Long newsId, HttpServletRequest request) {
        return newsService.deleteNews(newsId, request);
    }


}

