package com.whochucompany.byteclone.repository;

import com.whochucompany.byteclone.domain.comment.Comment;
import com.whochucompany.byteclone.domain.news.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment>  findAllByNews(News news);
}