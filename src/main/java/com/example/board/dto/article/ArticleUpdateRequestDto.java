package com.example.board.dto.article;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArticleUpdateRequestDto {
    private String title;
    private String content;

    // 새 이미지
    private List<MultipartFile> files = new ArrayList<>();
    private List<String> thumbnailYN = new ArrayList<>();

    // 기존 이미지
    private List<Long> existingImageIds = new ArrayList<>();
    private List<String> existingThumbnailYN = new ArrayList<>();
}