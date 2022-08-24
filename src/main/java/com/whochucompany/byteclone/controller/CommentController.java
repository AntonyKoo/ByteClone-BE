package com.whochucompany.byteclone.controller;

import com.whochucompany.byteclone.domain.comment.dto.CommentRequestDto;
import com.whochucompany.byteclone.domain.comment.dto.CommentResponseDto;
import com.whochucompany.byteclone.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor // 생성자 주입
@RestController
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/api/news/{newsId}/comment")
    public boolean createComment(@PathVariable Long newsId, @RequestBody CommentRequestDto requestDto, HttpServletRequest request){
        return commentService.createComment(newsId,requestDto, request);
    }

    @PutMapping("/api/news/{newsId}/{commentId}")
    public CommentResponseDto updateComment(@PathVariable Long newsId, @PathVariable Long commentId,
                                            @RequestBody CommentRequestDto requestDto, HttpServletRequest request){
        return commentService.updateComment(newsId,commentId,requestDto, request);
    }

    @DeleteMapping("/api/news/{newsId}/{commentId}")
    public boolean deleteComment(@PathVariable Long newsId, @PathVariable Long commentId, HttpServletRequest request){
        return commentService.deleteComment(newsId,commentId,request);
    }



}