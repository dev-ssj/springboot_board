package com.example.board.entity.article;

import com.example.board.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "image")
public class Image extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", updatable = false)
    private Long imageId;

    @Column(name="original_filename", nullable = false, updatable = false)
    private String originalFileName;

    @Column(name="s3_url", nullable = false, updatable = false)
    private String s3Url;

    @Column(name="image_size", nullable = false, updatable = false)
    private Long size;

    @Column(name="thumbnail_YN", nullable = false)
    @ColumnDefault("'N'")
    private String thumbnailYN;

    @Builder
    public Image(Long imageId, String originalFileName, String s3Url, Long size, String thumbnailYN){
        this.originalFileName = originalFileName;
        this.s3Url = s3Url;
        this.size = size;
        this.thumbnailYN = thumbnailYN;

    }

    public ImageBuilder toBuilder() {
        return builder()
                .imageId(this.imageId)  // imageId는 변경하지 않도록 설정
                .originalFileName(this.originalFileName)
                .s3Url(this.s3Url)
                .size(this.size)
                .thumbnailYN(this.thumbnailYN);  // 기존 thumbnailYN 값을 그대로 복사
    }
}
