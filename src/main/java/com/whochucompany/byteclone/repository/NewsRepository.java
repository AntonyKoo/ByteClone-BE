package com.whochucompany.byteclone.repository;

import com.whochucompany.byteclone.domain.news.News;
import com.whochucompany.byteclone.domain.news.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findAllByOrderByCreatedAtDesc();

    // 뉴스기사 아이디로 검색
    Optional<News> findByNewsId(Long newsId);

    // 페이지네이션 관련
    Page<News> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // newsType 별 페이지네이션
    Page<News> findAllByCategory(Category category, Pageable pageable);

    Page<News> findAllByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);
}
