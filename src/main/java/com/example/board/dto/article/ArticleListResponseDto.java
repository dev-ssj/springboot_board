package com.example.board.dto.article;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ArticleListResponseDto {
    private Long articleId;
    private String title;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private String email;
    private int views;

    @Builder
    public ArticleListResponseDto(Long articleId, String title, String thumbnailUrl, LocalDateTime createdAt, int views, String email){
        this.articleId = articleId;
        this.title =  title;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt =createdAt;
        this.views = views;
        this.email = email;
    }
}
