package com.example.board.dto.article;

import com.example.board.entity.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ArticleDetailResponseDto {
    private Long articleId;
    private String title;
    private String content;
    private int views;
    private LocalDateTime createAt;
    private List<ImageResponseDto> images;
    private String email;

    @Builder
    public ArticleDetailResponseDto(Long articleId, String title, String content, int views, LocalDateTime createAt, List<ImageResponseDto> images, String email){
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.views = views;
        this.createAt = createAt;
        this.images = images;
        this.email = email;
    }
}


