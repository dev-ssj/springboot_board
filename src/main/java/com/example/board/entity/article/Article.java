package com.example.board.entity.article;

import com.example.board.entity.common.BaseEntity;
import com.example.board.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id", updatable = false)
    private Long articleId;

    @Column(name ="title", nullable = false)
    private String title;

    @Column(name="content", nullable = false)
    private String content;

    @Column(name="views", nullable = false)
    @ColumnDefault("0")
    private int views = 0;

    @ManyToOne(fetch = FetchType.LAZY)  //단방향 관계. N+1문제를 방지하기 위해 Lazy로 설정
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="article_id")  //단방향관계. 외래키는 image쪽에
    private List<Image> images;

    @Builder
    public Article(Long articleId, String title, String content, List<Image> images, User user){
        this.title = title;
        this. content = content;
        this.views = 0;
        this.images = images;
        this.user = user;
    }

    //게시글 update 메서드
    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void changeImages(List<Image> newImages) {
        this.images.clear();         // 기존 이미지 제거 (orphanRemoval 적용됨)
        this.images.addAll(newImages);
    }
}
