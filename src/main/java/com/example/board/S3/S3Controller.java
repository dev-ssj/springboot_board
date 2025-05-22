package com.example.board.S3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class S3Controller {

    private final S3Service S3Service;

    //개별파일 업로드
    @PostMapping("/file")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam(value="file",required = false) MultipartFile multipartFile) {
        return ResponseEntity.ok(S3Service.uploadFile(multipartFile));
    }

    //다중파일 업로드
    @PostMapping("/multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(@RequestParam(value = "file", required = false) List<MultipartFile> multipartFiles) {
        return ResponseEntity.ok(S3Service.uploadMultiFile(multipartFiles));
    }
    //파일 삭제
    @DeleteMapping("/file")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam String fileName) {
        return ResponseEntity.ok(S3Service.deleteFile(fileName));
    }
}
