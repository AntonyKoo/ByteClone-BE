package com.whochucompany.byteclone.domain.comment;

import com.whochucompany.byteclone.domain.Timestamped;
import com.whochucompany.byteclone.domain.comment.dto.CommentRequestDto;
import com.whochucompany.byteclone.domain.member.Member;
import com.whochucompany.byteclone.domain.news.News;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Comment extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;


    @JoinColumn(name = "news_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private News news;

    @Column(nullable = false)
    private String comment;


    public void update(CommentRequestDto requestDto) {
        this.comment = requestDto.getComment();
    }
}