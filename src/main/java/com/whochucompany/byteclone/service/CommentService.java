package com.whochucompany.byteclone.service;

import com.whochucompany.byteclone.domain.comment.Comment;
import com.whochucompany.byteclone.domain.comment.dto.CommentRequestDto;
import com.whochucompany.byteclone.domain.comment.dto.CommentResponseDto;
import com.whochucompany.byteclone.domain.member.Member;
import com.whochucompany.byteclone.domain.news.News;
import com.whochucompany.byteclone.jwt.PrincipalDetails;
import com.whochucompany.byteclone.jwt.TokenProvider;
import com.whochucompany.byteclone.repository.CommentRepository;
import com.whochucompany.byteclone.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final NewsRepository newsRepository;
    private final CommentRepository commentRepository;
    private final TokenProvider tokenProvider;


    public boolean createComment(Long newsId, CommentRequestDto requestDto, HttpServletRequest request) {
        // 유효성 검사 항목 (토큰 유효성 검사)
        Member member = validateMember(request);
        Optional<News> optionalNews = newsRepository.findByNewsId(newsId);

        // 유효성 검사 실패시
        if(null==member){  // 멤버가 없을 시 null 반환됨. 그래서 바로 false;
            return false;
        }

        if(optionalNews.isEmpty()){ // 뉴스가 없을시 false 반환
            return false;
        }

        //성공시
        News news = optionalNews.get();

        Comment comment = Comment.builder()
                .member(member)
                .news(news)
                .comment(requestDto.getComment())
                .build();
        commentRepository.save(comment);
        return true;
    }

    public CommentResponseDto updateComment(Long newsId, Long commentId, CommentRequestDto requestDto, HttpServletRequest request) {
        // 유효성 검사 항목 (토큰 유효성 검사)
        Member member = validateMember(request);
        Optional<News> optionalNews = newsRepository.findByNewsId(newsId);

        // 유효성 검사 실패시
        if(null==member){  // 멤버가 없을 시 null 반환됨. 그래서 바로 false;
            throw new NullPointerException("해당 아이디가 없습니다.");
        }

        if(optionalNews.isEmpty()){ // 뉴스가 없을시 false 반환
            throw new NullPointerException("해당 뉴스가 없습니다.");
        }

        //성공시
        News news = optionalNews.get();

        Comment comment = commentRepository.findById(requestDto.getCommentId()).orElseThrow(() ->
                new RuntimeException("해당 코멘트가 없어요!"));

        comment.update(requestDto);
        return CommentResponseDto.builder()
                .id(comment.getId())
                .userName(comment.getMember().getUsername()) // 유저네임을 들고옴...
                .comment(comment.getComment())
                .build();
    }

    public boolean deleteComment(Long newsId, Long commentId, HttpServletRequest request) {
        // 유효성 검사 항목 (토큰 유효성 검사)
        Member member = validateMember(request);
        Optional<News> optionalNews = newsRepository.findByNewsId(newsId);

        // 유효성 검사 실패시
        if(null==member){  // 멤버가 없을 시 null 반환됨. 그래서 바로 false;
            return false;
        }

        if(optionalNews.isEmpty()){ // 뉴스가 없을시 false 반환
            return false;
        }

        commentRepository.deleteById(commentId);
        return true;

    }





    @Transactional
    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validationToken(request.getHeader("Authorization"))) {
            return null;
        }
        String accessToken = request.getHeader("Authorization");
        accessToken = resolveToken(accessToken);
        Member member = ((PrincipalDetails) (tokenProvider.getAuthentication(accessToken))).getMember();
        return member;
    }

    private String resolveToken(String token){
        if(token.startsWith("Bearer "))
            return token.substring(7);
        throw new RuntimeException("not valid refresh token !!");
    }


}