package com.example.board.dto.article;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageResponseDto {
    private Long imageId;
    private String originalFileName;
    private String s3Url;
    private long size;
    private String thumbnailYN;

    @Builder
    public ImageResponseDto(Long imageId, String originalFileName, String s3Url, long size, String thumbnailYN){
        this.imageId = imageId;
        this.originalFileName = originalFileName;
        this.s3Url =s3Url;
        this.size = size;
        this.thumbnailYN = thumbnailYN;
    }
}
