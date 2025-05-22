package com.example.board.service.article;

import com.example.board.S3.S3Service;
import com.example.board.dto.article.*;
import com.example.board.entity.article.Article;
import com.example.board.entity.article.Image;
import com.example.board.entity.user.User;
import com.example.board.repository.article.ArticleRepository;
import com.example.board.repository.article.ImageRepository;
import com.example.board.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ImageRepository imageRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;


    /*
    *게시글 저장(다중 이미지 저장)
     */
    @Transactional
    public Map<String, Object> saveArticle(ArticleSaveDto requestDto, List<MultipartFile> files, List<String> thumbnailYNs, String email) {
        //이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("유저를 찾을 수 없습니다 : " + email));

        List<Image> imageEntities = new ArrayList<>();
        //이미지의 실제 저장 장소를 담을 객체
        List<String> imageUrls = new ArrayList<>();

        //파일이 존재한다면
        if (files != null && !files.isEmpty()) {
            //*업로드된 파일을 s3에 저장 -> 반환값은 succes, data(성공 여부, 업로드된 파일명 리스트)*
            Map<String, Object> uploadResult = s3Service.uploadMultiFile(files);
            
            //uploadResult의 success가 true이면(업로드가 성공했으면)
            if (Boolean.TRUE.equals(uploadResult.get("success"))) {
                //실제 s3에 저장된 파일명의 리스트를 받는다.
                List<String> fileNames = (List<String>) uploadResult.get("data");

                //파일 갯수만큼
                for (int i = 0; i < fileNames.size(); i++) {
                    //MultipartFile 타입으로 파일을 받아오고
                    MultipartFile originalFile = files.get(i);
                    //파일명을 가져오고
                    String s3FileName = fileNames.get(i);
                    //파일의 url을 지정
                    String s3Url = "https://" + bucket + ".s3.amazonaws.com/" + s3FileName;

                    //thumbnailYN 값 가져오기
                    String thumbnailYN = thumbnailYNs.get(i);

                    //빌더를 이용하여 Image객체에 저장
                    Image image = Image.builder()
                            .originalFileName(originalFile.getOriginalFilename())
                            .s3Url(s3Url)
                            .size(originalFile.getSize())
                            .thumbnailYN(thumbnailYN)
                            .build();

                    // List<Image> imageEntities에 image 객체 저장
                    imageEntities.add(image);
                    //List<String> imageUrls에 파일 url 저장
                    imageUrls.add(s3Url);
                }
            } else {
                //업로드 실패 시 예외처리
                throw new RuntimeException("이미지 업로드 실패: " + uploadResult.get("message"));
            }
        }

        //toEntity를 통해 게시글 엔티티생성(이미지 리스트와 user정보 포함)-> title, content는 Controller에서 받아옴
        Article article = requestDto.toEntity(imageEntities,user);
        //게시글 DB에 저장
        articleRepository.save(article);

        // ✅ 응답에 articleId와 imageUrls 포함
        Map<String, Object> result = new HashMap<>();
        result.put("articleId", article.getArticleId());
        result.put("imageUrls", imageUrls);

        return result;
    }

    /*
     *게시글 목록 조회
     */
    public List<ArticleListResponseDto> findAll(){
        //articleId를 기준으로 내림차순 정렬하여 모든 게시글 가져오기.
        List<Article> articles = articleRepository.findAll(Sort.by(Sort.Direction.DESC,"articleId"));

        //articles을 Stream으로 변환해서 처리
        return articles.stream()
                .map(article -> {   //map : 안에 있는 각각의 요소를 다른 형태로 변환하는것(mapping)
                    //썸네일 이미지 찾기.(thumbnailUrl에는 썸네일 이미지의 s3Url이 들어가거나, 없으면 null이 들어감
                    String thumbnailUrl = article.getImages().stream()//이미지 리스트를 stream으로 변환
                            .filter(image -> "Y".equalsIgnoreCase(image.getThumbnailYN()))  //getThumbnailYN이 Y인 이미지만 필터링
                            .map(Image::getS3Url)//필터링된 Image에서 S3Url 꺼냄
                            .findFirst()    //첫번째 이미지의 s3url만 선택
                            .orElse(null);  //만약 없으면 null 반환

                    //ArticleListResponseDto 객체로 변환
                return ArticleListResponseDto.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .thumbnailUrl(thumbnailUrl)
                        .createdAt(article.getCreatedAt())
                        .views(article.getViews())
                        .email(article.getUser().getEmail())
                        .build();
                })//각각의 article을 ArticleListResponseDto로 변환
                .collect(Collectors.toList()); //결과를 List형태로 바꿈
    }

    /*
     * 게시글 조회
     */
    //엔티티반환. 내부용
    @Transactional
    public Article findById(long id){
        articleRepository.updateViews(id);
        return articleRepository.findById(id).orElse(null);
    }

    //dto반환. view, api용 
    public ArticleDetailResponseDto getArticleDetailDto(Long articleId){
        Article article = findById(articleId);
        if(article == null) return null;    //해당 article이 없으면 null 반환

        return ArticleDetailResponseDto.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .content(article.getContent())
                .views(article.getViews())
                .createAt(article.getCreatedAt())
                .images(article.getImages().stream()
                        .map(image -> ImageResponseDto.builder()
                                .imageId(image.getImageId())
                                .originalFileName(image.getOriginalFileName())
                                .s3Url(image.getS3Url())
                                .size(image.getSize())
                                .thumbnailYN(image.getThumbnailYN())
                                .build())
                        .collect(Collectors.toList()))
                .email(article.getUser().getEmail())
                .build();
    }

    /*
     * 게시글 삭제
     */
    @Transactional
    public void deleteArticle(long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
        }
        Article article = findById(articleId);

        //originalFileName은 원본 파일명이고, S3에 저장된 실제 파일명은
        //UUID+확장자로 만들어진 랜덤파일 이므로 S3Url에서 파일명을 추출해야한다.
        List<String> imageNames = article.getImages().stream()
                .map(image->extractFileNameFromUrl(image.getS3Url()))
                .toList();

        for(String fileName : imageNames){
            s3Service.deleteFile(fileName);
        }

        articleRepository.deleteById(articleId);
    }

    //S3Url에서 파일명 추출
    //url.lastIndexOf("/") : 마지막 /의 위치부터 추출 -> 결과 : /abc1234-filename.jpg
    //+1을 해서 /다음부터 잘라냄.
    private String extractFileNameFromUrl(String url){
        return url.substring(url.lastIndexOf("/")+1);
    }

    /*
    * 게시글 수정
    */
    @Transactional
    public ResponseDto<ArticleSaveDto> updateArticle(Long articleId, ArticleUpdateRequestDto dto) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        List<Image> updatedImages = new ArrayList<>();

        // 기존 이미지 유지
        Set<Long> existingImageIdsSet = new HashSet<>(Optional.ofNullable(dto.getExistingImageIds()).orElse(Collections.emptyList()));
        List<String> existingThumbnails = Optional.ofNullable(dto.getExistingThumbnailYN()).orElse(Collections.emptyList());

        for (int i = 0; i < existingImageIdsSet.size(); i++) {
            Long id = dto.getExistingImageIds().get(i);
            String thumbnail = existingThumbnails.get(i);

            Image image = imageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("이미지 없음"));

            updatedImages.add(image.toBuilder().thumbnailYN(thumbnail).build());
        }

        // 새 이미지 업로드
        List<String> newThumbnailYN = Optional.ofNullable(dto.getThumbnailYN()).orElse(Collections.emptyList());
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            Map<String, Object> uploadResult = s3Service.uploadMultiFile(dto.getFiles());
            if (Boolean.TRUE.equals(uploadResult.get("success"))) {
                List<String> fileNames = (List<String>) uploadResult.get("data");

                for (int i = 0; i < fileNames.size(); i++) {
                    MultipartFile file = dto.getFiles().get(i);
                    String s3Url = "https://" + bucket + ".s3.amazonaws.com/" + fileNames.get(i);
                    String thumbnailYN = (i < newThumbnailYN.size()) ? newThumbnailYN.get(i) : "N";

                    Image newImage = Image.builder()
                            .originalFileName(file.getOriginalFilename())
                            .s3Url(s3Url)
                            .size(file.getSize())
                            .thumbnailYN(thumbnailYN)
                            .build();
                    updatedImages.add(newImage);
                }
            }
        }

        // 삭제할 이미지 제거 (DB & S3)
        for (Image img : new ArrayList<>(article.getImages())) {
            if (!existingImageIdsSet.contains(img.getImageId())) {
                s3Service.deleteFile(extractFileNameFromUrl(img.getS3Url()));
            }
        }

        // 게시글 정보 수정
        article.updateContent(dto.getTitle(), dto.getContent());
        article.changeImages(updatedImages);

        // 수정된 게시글 요약 정보 응답 DTO로 구성
        ArticleSaveDto responseDto = ArticleSaveDto.builder()
                .articleId(article.getArticleId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrls(updatedImages.stream().map(Image::getS3Url).toList())
                .build();

        return ResponseDto.<ArticleSaveDto>builder()
                .status(200)
                .message("게시글 수정 성공")
                .data(responseDto)
                .build();
    }

}