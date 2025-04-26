package com.project.webBuilder.global.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;  // 성공 여부
    private String message;   // 메시지
    private T data;           // 실제 데이터 (제네릭 타입)

    // 성공적인 응답 생성자 (데이터만)
    public ApiResponse(T data) {
        this.success = true;
        this.message = "요청이 성공적으로 처리되었습니다.";  // 기본 성공 메시지
        this.data = data;
    }

    // 성공적인 응답 생성자 (메시지 + 데이터)
    public ApiResponse(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }
}
