package com.github.p3.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 이메일
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "잘못된 이메일 형식입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이메일입니다."),


    // 닉네임
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 2~10자, 한글, 영문 또는 숫자를 포함해야 합니다."),

    // 비밀번호
    INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 8~20자, 영문 대소문자 중 하나와 숫자를 포함해야 합니다."),

    // 탈퇴
    ACCOUNT_DEACTIVATED(HttpStatus.BAD_REQUEST, "비활성화된 계정입니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않은 상품입니다."),

    // 권한 관련 오류
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "권한이 없는 접근입니다.");

    private final HttpStatus status;
    private final String message;
}