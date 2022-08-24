package com.whochucompany.byteclone.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.whochucompany.byteclone.controller.ResponseDto;
import com.whochucompany.byteclone.domain.comment.Comment;
import com.whochucompany.byteclone.domain.comment.dto.CommentListDto;
import com.whochucompany.byteclone.domain.member.Member;
import com.whochucompany.byteclone.domain.news.News;
import com.whochucompany.byteclone.domain.news.dto.NewsDetailResponseDto;
import com.whochucompany.byteclone.domain.news.dto.NewsRequestDto;
import com.whochucompany.byteclone.domain.news.dto.NewsResponseDto;
import com.whochucompany.byteclone.domain.news.enums.Category;
import com.whochucompany.byteclone.domain.news.enums.View;
import com.whochucompany.byteclone.jwt.TokenProvider;
import com.whochucompany.byteclone.repository.CommentRepository;
import com.whochucompany.byteclone.repository.NewsRepository;
import com.whochucompany.byteclone.util.ImageUrlProccessingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final AmazonS3Client amazonS3Client;
    private final CommentRepository commentRepository;

    private final TokenProvider tokenProvider;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 뉴스 작성
    @Transactional
    public ResponseDto<?> createNews(NewsRequestDto requestDto, HttpServletRequest request) throws IOException {
        // 회원 로그인 확인 로직
        Member member = validateMember(request);

        if(null == member) {
            throw new NullPointerException("회원만 사용 가능합니다.");
        }

        View view = null;
        Category category = null;

        // 읽기 권한을 구분하는 로직
        try {
            view = View.valueOf(requestDto.getView());
        } catch (IllegalArgumentException e) {
            return ResponseDto.fail("BAD_REQUEST", "VIEW에 없는 항목입니다.");
        }

        // 기사 주제를 구분하는 로직
        try {
            category = Category.valueOf(requestDto.getCategory());
        } catch (IllegalArgumentException e) {
            return ResponseDto.fail("BAD_REQUEST", "CATEGORY에 없는 항목입니다.");
        }

        String result = null;

        // 이미지 파일이 비어있지 않은 경우, 작동하는 로직
        if (!requestDto.getImage().isEmpty()) {
            String fileName = ImageUrlProccessingUtils.buildFileName(requestDto.getImage().getOriginalFilename());

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(requestDto.getImage().getContentType());

            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, requestDto.getImage().getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            // S3 업로드 후, 저장된 url을 result에 넣어줌
            result = amazonS3Client.getUrl(bucketName, fileName).toString();

        }

        // ** 추가 필요, Member는 회원 검증 로직에서 가져옴


        News news = News.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent()) // 얘는 크기 크니깐 return 주지 말까?
                .image(result)
                .view(view)
                .member(member)
                .category(category)
                .build();
        newsRepository.save(news);



        NewsResponseDto newsResponseDto =
                NewsResponseDto.builder().title(news.getTitle())
                        .image(news.getImage())
                        .content(news.getContent())
                        .view(news.getView())
                        .category(news.getCategory())
                        .createdAt(news.getCreatedAt())
                        .build();
        return ResponseDto.success(newsResponseDto);
    }

    // 업데이트
    @Transactional
    public ResponseDto<?> updateNews(Long newsId, NewsRequestDto requestDto, HttpServletRequest request) throws IOException {

        // 회원 토큰 검증 로직
        Member member = validateMember(request);
        if (null == member) {
            throw new NullPointerException("회원만 사용 가능합니다.");
        }

        // news 게시글 존재 유무 확인 로직
//        News news = newsRepository.findByNewsId(newsId); // 왜 redundant?
        Optional<News> optionalNews = newsRepository.findByNewsId(newsId);

        News news = optionalNews.get();
        if (null == news) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 news ID 입니다.");
        }

        // 회원 정보 가져와서 작성자 검증
        if (!news.getMember().getId().equals(member.getId())) {
            return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
        }

        // 읽기 권한 카테고리 수정 로직
        View view = news.getView();
        try {
            view = View.valueOf(requestDto.getView());

        } catch (IllegalArgumentException e) {
            return ResponseDto.fail("BAD_REQUEST", "VIEW에 없는 항목입니다.");
        }

        Category category = news.getCategory();
        try {
            category = Category.valueOf(requestDto.getCategory());
        } catch (IllegalArgumentException e) {
            return ResponseDto.fail("BAD_REQUEST", "CATEGORY에 없는 항목입니다.");
        }

        String result = news.getImage();

        if (!requestDto.getImage().isEmpty()) {
            String fileName = ImageUrlProccessingUtils.buildFileName(requestDto.getImage().getOriginalFilename());

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(requestDto.getImage().getContentType());

            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, requestDto.getImage().getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            result = amazonS3Client.getUrl(bucketName, fileName).toString();
        }

        news.updateNews(requestDto, result);
        return ResponseDto.success(NewsResponseDto.builder()
                .newsId(news.getNewsId())
                .title(news.getTitle())
                .username(news.getMember().getUsername())
                .image(news.getImage())
                .view(news.getView())
                .category(news.getCategory())
                .createdAt(news.getCreatedAt())
                .build()
        );
    }

    // 조회: 전체 조회 + 상세 조회 and 전체조회는 비회원, 카테고리별 로 구성됨
    // 전체 조회 = 회원 + 비회원 모두 열람 가능, (유료+무료) 전체 기사가 조회됨
    @Transactional(readOnly = true)
    public Page<NewsResponseDto> findAll(Pageable pageable) {
        Page<News> newsList = newsRepository.findAllByOrderByCreatedAtDesc(pageable);
        Page<NewsResponseDto> newsResponseDtos = convertToResponseDto(newsList);

        return newsResponseDtos;
    }

    private Page<NewsResponseDto> convertToResponseDto(Page<News> newslist) {
        List<NewsResponseDto> newsList = new ArrayList<>();

        for (News news : newslist) {
            newsList.add(
                    NewsResponseDto.builder()
                            .newsId(news.getNewsId())
                            .title(news.getTitle())
                            .username(news.getMember().getUsername())
                            .image(news.getImage())
                            .view(news.getView())
                            .category(news.getCategory())
                            .createdAt(news.getCreatedAt())
                            .build()
            );
        }
        return new PageImpl(newsList, newslist.getPageable(), newslist.getTotalElements());
    }

    // Category 별 전체 조회
    @Transactional(readOnly = true)
    public Page<NewsResponseDto> findAllByCategory(Category category, Pageable pageable) {
        System.out.println("category = " + category);

        Page<News> newsList = newsRepository.findAllByCategoryOrderByCreatedAtDesc(category, pageable);

        Page<NewsResponseDto> newsResponseDtos = convertToResponseDto(newsList);

        return newsResponseDtos;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> readDetailNews(Long newsId) {
        Optional<News> optionalNews = newsRepository.findByNewsId(newsId);
        if(optionalNews.isEmpty()){
            throw new NullPointerException("해당 게시글이 없습니다.");
        }
        News news = optionalNews.get();

        List<Comment> commentList = commentRepository.findAllByNews(news);
        List<CommentListDto> commentResponseDtoList = new ArrayList<>();
        for(Comment comment  : commentList){
            commentResponseDtoList.add(
                CommentListDto.builder()
                        .id(comment.getId())
                        .comment(comment.getComment())
                        .username(comment.getMember().getUsername())
                        .createAt(comment.getCreatedAt())
                        .build());
        }

        NewsDetailResponseDto newsDetailResponseDto = NewsDetailResponseDto.builder()
                .newsId(news.getNewsId())
                .title(news.getTitle())
                .username(news.getMember().getUsername())
                .image(news.getImage())
                .view(news.getView())
                .category(news.getCategory())
                .createdAt(news.getCreatedAt())
                .content(news.getContent())
                .commentList(commentResponseDtoList)
                .build();

        return new ResponseEntity<>(newsDetailResponseDto, HttpStatus.OK);
    }



    @Transactional
    public ResponseDto<?> deleteNews(Long newsId, HttpServletRequest request) {
        // 회원 토큰 검증 로직
        Member member = validateMember(request);
        if (null == member) {
            throw new NullPointerException("회원만 사용 가능합니다.");
        }

        // news 게시글 존재 유무 확인 로직
        Optional<News> optionalNews = newsRepository.findByNewsId(newsId);

        if (optionalNews.isEmpty()) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 news ID 입니다.");
        }
        News news = optionalNews.get();  // orElseThorw New~~

        // 회원 정보 가져와서 작성자 검증
        if (!news.getMember().getId().equals(member.getId())) {
            return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
        }

        newsRepository.deleteById(newsId);

        return ResponseDto.success(true);
    }

    @Transactional
    public Member validateMember(HttpServletRequest request) {
        String accessToken = resolveToken(request.getHeader("Authorization"));
        if (!tokenProvider.validationToken(accessToken)) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }

    private String resolveToken(String token){
        if(token.startsWith("Bearer "))
            return token.substring(7);
        throw new RuntimeException("not valid refresh token !!");
    }

}
