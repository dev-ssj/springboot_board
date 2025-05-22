package com.example.board.S3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");

    //단일 이미지 업로드
    public Map<String, Object> uploadFile(MultipartFile multipartFile) {
        Map<String, Object> response = new HashMap<>();

        //파일이 비어있으면 실패 응답 반환
        if (multipartFile == null || multipartFile.isEmpty()) {
            response.put("success", false);
            response.put("message", "파일이 비어 있습니다.");
            return response;
        }
        
        String fileName = createFileName(multipartFile.getOriginalFilename());  //파일의 실제이름
        //ObjectMetadata : S3에 업로드되는 파일과 관련된 정보 설정 객체(크기, 타입 등등)
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());   //obejctMetadata에 파일의 사이즈 저장
        objectMetadata.setContentType(multipartFile.getContentType());  //파일의 타입 저장

        /*
        InputStream() : 바이트 입력 스트림을 나타내는 모든 클래스의 슈퍼클래스
        getInputStream() : 파일의 내용을 읽기 위한 InputStream을 반환
        1. multipartFile로부터 데이터를 읽은 후 PutObjectRequest 객체를 생성한다.
        2. 이때 파일을 외부에서 읽을 수 있도록 하기 위해 withCannedAcl을 설정한다.(개별 파일에 cannedAcl(사전 정의된 ACL)을 적용한다는 의미이다.)
        3. putObject메소드를 이용해 생성된 객체를 AWS S3로 전송한다.
         */
        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            String fileUrl = amazonS3.getUrl(bucket, fileName).toString();

            response.put("success", true);
            response.put("message", "이미지가 성공적으로 업로드 되었습니다.");
            response.put("data", fileName);
            response.put("fileUrl", fileUrl);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "파일 업로드에 실패했습니다.");
        }

        return response;
    }

    //다중 파일 업로드
    public Map<String, Object> uploadMultiFile(List<MultipartFile> multipartFiles){
        Map<String, Object> response = new HashMap<>();
        List<String> fileNameList = new ArrayList<>();
        List<String> fileUrlList = new ArrayList<>();

        if(multipartFiles == null || multipartFiles.isEmpty()){
            response.put("success", false);
            response.put("message", "파일이 비어 있습니다.");
        }

        try{
            // forEach 구문을 통해 multipartFiles 리스트로 넘어온 파일들을 순차적으로 fileNameList 에 추가
            for (MultipartFile file : multipartFiles) {
                String fileName = createFileName(file.getOriginalFilename());
                String fileUrl = amazonS3.getUrl(bucket, fileName).toString();  //파일 url
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(file.getSize());
                objectMetadata.setContentType(file.getContentType());

                try (InputStream inputStream = file.getInputStream()) {
                    amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                fileNameList.add(fileName);
                fileUrlList.add(fileUrl);

                System.out.println(fileNameList.toString());
                System.out.println(fileUrlList.toString());
            }

            response.put("success",true);
            response.put("message","이미지가 성공적으로 업로드 되었습니다.");
            response.put("data" ,fileNameList);
            response.put("fileUrl", fileUrlList);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다.");
        }
        return response;
    }

    //다중파일 업로드 : 폴더 지정
    public Map<String, Object> uploadMultiFile(List<MultipartFile> multipartFiles, List<String> customFileNames) {
        Map<String, Object> response = new HashMap<>();
        List<String> uploadedFileNames = new ArrayList<>();

        if (multipartFiles == null || multipartFiles.isEmpty()) {
            response.put("success", false);
            response.put("message", "파일이 비어 있습니다.");
            return response;
        }

        if (multipartFiles.size() != customFileNames.size()) {
            response.put("success", false);
            response.put("message", "파일 수와 파일명 수가 일치하지 않습니다.");
            return response;
        }

        try {
            for (int i = 0; i < multipartFiles.size(); i++) {
                MultipartFile file = multipartFiles.get(i);
                String fileName = customFileNames.get(i);

                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(file.getSize());
                objectMetadata.setContentType(file.getContentType());

                try (InputStream inputStream = file.getInputStream()) {
                    amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
                } catch (IOException e) {
                    throw new RuntimeException("파일 업로드 실패: " + fileName, e);
                }

                uploadedFileNames.add(fileName);
            }

            response.put("success", true);
            response.put("message", "이미지가 성공적으로 업로드 되었습니다.");
            response.put("data", uploadedFileNames);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류 발생: " + e.getMessage());
        }

        return response;
    }

    // 파일명을 난수화하기 위해 UUID 를 활용.(랜덤 난수 + 파일의 확장자명 : errtrtf3-dfdf3.jpg)
    public String createFileName(String fileName){
        return UUID.randomUUID().toString().concat(getFileCommaExtension(fileName));
    }

    //  확장자 추출 메서드(a.jpg -> .jpg). 확장자가 없으면 예외처리
    private String getFileCommaExtension(String fileName){
        try{
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일" + fileName + ") 입니다.");
        }
    }

    private String getFileExtension(String fileName){
        try {
            String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "허용되지 않는 파일 확장자입니다: " + extension);
            }

            return extension;
        } catch (StringIndexOutOfBoundsException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "잘못된 형식의 파일입니다: " + fileName);
        }
    }

    //파일 삭제
    public Map<String, Object> deleteFile(String fileName) {
        Map<String, Object> response = new HashMap<>();

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
            response.put("success", true);
            response.put("message", "이미지가 정상적으로 삭제되었습니다.");
            response.put("data", fileName);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "파일 삭제에 실패했습니다.");
        }

        return response;
    }
}