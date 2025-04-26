package com.project.webBuilder.global.exeption.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "사용자가 인증되지 않았습니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),
    DEPLOY_NOT_FOUND(HttpStatus.NOT_FOUND,"배포 경로를 찾을 수 없습니다.");

    private final HttpStatus status;  // 오류 코드
    private final String message; // 오류 메시지

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
