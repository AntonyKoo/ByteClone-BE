package com.whochucompany.byteclone.domain.news;

import com.whochucompany.byteclone.domain.Timestamped;
import com.whochucompany.byteclone.domain.comment.Comment;
import com.whochucompany.byteclone.domain.member.Member;
import com.whochucompany.byteclone.domain.news.dto.NewsRequestDto;
import com.whochucompany.byteclone.domain.news.enums.Category;
import com.whochucompany.byteclone.domain.news.enums.View;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class News extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long newsId;
    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)  // 담기는 글자 양이 많아서 Lob을 줌
    private String content;

    @Column(nullable = true)
    private String image;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private View view;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    // 작성자
    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    //댓글
    @OneToMany(mappedBy = "news", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList;

    public void updateNews(NewsRequestDto newsRequestDto, String image) {
        this.title = newsRequestDto.getTitle();
        this.content = newsRequestDto.getContent();
        this.image = image;
        this.view = View.valueOf(newsRequestDto.getView());
        this.category = Category.valueOf(newsRequestDto.getCategory());
    }

}
