package com.example.board.controller.article;

import com.example.board.dto.article.ArticleDetailResponseDto;
import com.example.board.dto.article.ArticleListResponseDto;
import com.example.board.entity.article.Article;
import com.example.board.service.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ArticleViewController {

    private final ArticleService articleService;

    @GetMapping("/index")
    public String testView(){
        return "index";
    }

    @GetMapping("/new")
    public String newArticle(){ return "article/newArticle";}

    /*
    * 글 목록 조회
    */
    @GetMapping("/list")
    public String listArticle(Model model, Principal principal) {
       List<ArticleListResponseDto> articles = articleService.findAll();
       model.addAttribute("articles", articles);
       model.addAttribute("userEmail", principal.getName());

        return "article/articleList";
    }


    /*
     * 글 상세 조회
     */
    @GetMapping("/articles/{articleId}")
    public String getArticle(@PathVariable Long articleId, Model model, Principal principal){
        ArticleDetailResponseDto article = articleService.getArticleDetailDto(articleId);
        model.addAttribute("article", article);
        model.addAttribute("userEmail", principal.getName());
        return "article/articleView";
    }

    /*
    * 글 수정 폼 조회
    */
    @GetMapping("/edit/{articleId}")
    public String editArticle(@PathVariable Long articleId, Model model, Principal principal){
        ArticleDetailResponseDto article = articleService.getArticleDetailDto(articleId);

        // 작성자 이메일과 로그인한 사용자 이메일 비교
        if (!article.getEmail().equals(principal.getName())) {
            return "redirect:/list";
        }

        model.addAttribute("article", article);
        return "article/articleUpdate";
    }

}
