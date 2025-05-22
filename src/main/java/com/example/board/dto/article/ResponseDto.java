package com.example.board.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResponseDto<T> {
    private int status;      // HTTP 상태코드 (예: 200, 400)
    private String message;  // 설명 메시지
    private T data;          // 실제 응답 데이터
}
