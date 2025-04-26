package com.project.webBuilder.global.exeption.custom;

import com.project.webBuilder.global.exeption.errorcode.ErrorCode;
import org.springframework.http.HttpStatus;


public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;  // 오류 코드 (enum 사용)
    private final String details;       // 추가적인 오류 상세 정보

    // 오류 코드와 메시지를 전달하는 생성자
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;  // 기본적으로 추가 정보는 null
    }

    // 오류 코드, 메시지, 상세 정보를 전달하는 생성자
    public CustomException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    // Getter
    public String getErrorCode() {
        return errorCode.name();
    }

    public String getDetails() {
        return details;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus(); // 예외에 해당하는 상태 코드 반환
    }
}
