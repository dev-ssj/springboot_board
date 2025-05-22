package com.example.board.repository.article;

import com.example.board.entity.article.Article;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Transactional
    @Modifying //조회수
    @Query("UPDATE Article a set a.views = a.views+1 where a.articleId = :articleId")
    void updateViews(@Param("articleId") Long articleId);
}
