package com.example.board.dto.article;


import com.example.board.entity.article.Article;
import com.example.board.entity.article.Image;
import com.example.board.entity.user.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ArticleSaveDto {

    private Long articleId;

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    private String title;

    @NotBlank(message = "본문은 필수 입력 값입니다.")
    private String content;

    private List<String> imageUrls;

    private User user;


    @Builder
    public ArticleSaveDto(Long articleId, String title, String content, List<String> imageUrls, User user){
        this.articleId = articleId;
        this.title =title;
        this.content = content;
        this.imageUrls = imageUrls;
        this.user = user;

    }

    public Article toEntity(List<Image> images, User user) {
        return Article.builder()
                .title(this.title)
                .content(this.content)
                .images(images)
                .user(user)
                .build();
    }
}
