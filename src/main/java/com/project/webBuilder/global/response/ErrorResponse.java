package com.project.webBuilder.global.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse {
    private String code;           // 에러 코드 (예: USER_NOT_FOUND)
    private String message;        // 에러 메시지
    private int status;            // HTTP 상태 코드 (예: 404)
    private LocalDateTime timestamp;  // 에러 발생 시간
    private String details;        // 오류 상세 정보 (optional)

    // 에러 발생 시 생성자
    public ErrorResponse(String code, String message, int status, String details) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();  // 현재 시간
        this.details = details;
    }

}
