package com.project.webBuilder.global.exeption.handler;

import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        // CustomException의 오류 코드와 메시지를 사용하여 ErrorResponse 생성
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                ex.getDetails()
        );
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    // 500 Internal Server Error 처리 (예상치 못한 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        // 서버 오류 시, ErrorCode 대신 하드코딩된 메시지 처리
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",  // 코드
                "서버에서 오류가 발생했습니다.",  // 메시지
                HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 상태 코드
                cause.getMessage()  // 상세 메시지
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
