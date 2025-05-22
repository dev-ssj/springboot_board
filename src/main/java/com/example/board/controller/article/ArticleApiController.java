package com.example.board.controller.article;

import com.example.board.S3.S3Service;
import com.example.board.dto.article.*;
import com.example.board.service.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ArticleApiController {
    private final ArticleService articleService;
    private final S3Service s3Service;

    //글 등록 테스트
    @PostMapping("/api/submit")
    public ResponseEntity<String> handleFileUpload(@RequestParam("files") List<MultipartFile> files, @RequestParam("title") String title, @RequestParam("content") String content) {
        // 파일과 다른 데이터 처리
        System.out.println("파일 개수: " + files.toArray().length);
        for (MultipartFile file : files) {
            System.out.println("파일 이름: " + file.getOriginalFilename());
            // 파일 저장 로직 추가
        }

        System.out.println("제목: " + title);
        System.out.println("본문: " + content);

        return ResponseEntity.ok("게시글 등록 완료");
    }

    /*
    * 글 등록
    */
    @PostMapping("/api/save")
    public ResponseEntity<ResponseDto<ArticleSaveDto>> saveArticle(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "files") List<MultipartFile> files,
            @RequestParam(value = "thumbnailYN") List<String> thumbnailYNs,
            Principal principal
    ) {

        String email = principal.getName(); //로그인 사용자의 이메일 추출

        //클라이언트로부터 받은 값들로 게시글 DTO객체 생성
        ArticleSaveDto requestDto = ArticleSaveDto.builder()
                .title(title)
                .content(content)
                .build();

        try {
            //requestDto(title과 content가 담김)와 files를 파라미터로 서비스계층의 saveArticle 호출(이때 db에 저장됨!)
            Map<String, Object> result = articleService.saveArticle(requestDto, files, thumbnailYNs,email);
            //저장된 게시글 id와 이미지 url을 리스트로 추출
            Long articleId = (Long) result.get("articleId");
            List<String> imageUrls = (List<String>) result.get("imageUrls");

            //응답 DTO 구성 (저장성공 여부, 게시글 id, 제목, 본문, 이미지url 리스트)
            ArticleSaveDto articleSaveDto = ArticleSaveDto.builder()
                    .articleId(articleId)
                    .title(title)
                    .content(content)
                    .imageUrls(imageUrls)
                    .build();

            //성공 응답
            ResponseDto<ArticleSaveDto> response = ResponseDto.<ArticleSaveDto>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("게시글 저장 성공")
                    .data(requestDto)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        //게시글 저장중 예외가 발생하면 실패 응답
        } catch (Exception e) {
           ResponseDto<ArticleSaveDto> error = ResponseDto.<ArticleSaveDto>builder()
                   .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                   .message("게시글 저장 중 오류 발생 : " + e.getMessage())
                   .data(null)
                   .build();
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /*
    * 글 목록 조회 
    */
    @GetMapping("/api/articles")
    public ResponseEntity<ResponseDto<List<ArticleListResponseDto>>> getAllArticles(){
        List<ArticleListResponseDto> articleListDtos = articleService.findAll();

        ResponseDto<List<ArticleListResponseDto>> responseDto = ResponseDto.<List<ArticleListResponseDto>>builder()
                .status(HttpStatus.OK.value())
                .message("게시글 목록 조회 성공")
                .data(articleListDtos)
                .build();

        return ResponseEntity.ok(responseDto);
    }


    /*
    * 글 상세 조회
    */
    @GetMapping("/api/articles/{articleId}")
    public ResponseEntity<ResponseDto<ArticleDetailResponseDto>> getArticle(@PathVariable("articleId")Long articleId){
        ArticleDetailResponseDto article = articleService.getArticleDetailDto(articleId);

        //해당 id의 글이 없으면
        if(article == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseDto.<ArticleDetailResponseDto>builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message("해당 게시글이 없습니다.")
                            .data(null)
                            .build()
            );
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseDto.<ArticleDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("게시글 조회 성공")
                        .data(article)
                        .build()
        );

    }

    /*
     *  게시글 삭제
     */
    @DeleteMapping("/api/articles/{articleId}")
    public ResponseEntity<ResponseDto<Long>> deleteArticle(@PathVariable("articleId") Long articleId){
        try{
            articleService.deleteArticle(articleId);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ResponseDto.<Long>builder()
                            .status(HttpStatus.OK.value())
                            .message("게시글 삭제 성공")
                            .data(articleId)
                            .build()
            );
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseDto.<Long>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("게시글 삭제 오류 : "+ e.getMessage())
                            .build()
            );
        }
    }

    // 게시글 수정 API (이미지 수정, 추가, 삭제 처리 포함)
    @PostMapping("/api/update/{articleId}")
    public ResponseEntity<ResponseDto<ArticleSaveDto>> updateArticle(
            @PathVariable Long articleId,
            @ModelAttribute ArticleUpdateRequestDto updateDto
    ) {
        try {
            ResponseDto<ArticleSaveDto> result = articleService.updateArticle(articleId, updateDto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    ResponseDto.<ArticleSaveDto>builder()
                            .status(500)
                            .message("게시글 수정 중 오류 발생: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }
}