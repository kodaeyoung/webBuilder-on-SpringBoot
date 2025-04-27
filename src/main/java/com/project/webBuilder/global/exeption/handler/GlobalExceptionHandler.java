package com.project.webBuilder.global.exeption.handler;

import com.project.webBuilder.global.exeption.custom.CustomException;
import com.project.webBuilder.global.response.ErrorResponse;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
@Slf4j
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

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        // 1. 에러를 서버 로그에 기록
        log.error("파일 처리 중 오류 발생", ex);

        // 2. 클라이언트에 보낼 에러 응답 (상세 원인은 보내지 않음)
        ErrorResponse errorResponse = new ErrorResponse(
                "FILE_SYSTEM_ERROR",                     // 에러 코드
                "파일 처리 중 오류가 발생했습니다.",           // 사용자에게 보여줄 간단한 메시지
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 상태코드
                ex.getMessage()                                      // 상세 원인은 보내지 않음
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 500 Internal Server Error 처리 (예상치 못한 예외)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("예상치 못한 오류 발생", ex);
        // 서버 오류 시 하드코딩된 메시지 처리
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",  // 코드
                "서버에서 오류가 발생했습니다.",  // 메시지
                HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 상태 코드
                ex.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
