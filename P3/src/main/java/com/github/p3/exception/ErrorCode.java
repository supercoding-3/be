package com.github.p3.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 이메일
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "Bad Request", "잘못된 이메일 형식입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "Conflict", "이미 사용 중인 이메일입니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "존재하지 않는 이메일입니다."),

    // 닉네임
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "Conflict", "이미 사용 중인 닉네임입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "Bad Request", "닉네임은 2~10자, 한글, 영문 또는 숫자를 포함해야 합니다."),

    // 비밀번호
    INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "Unauthorized", "비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "Bad Request", "비밀번호는 8~20자, 영문 대소문자 중 하나와 숫자를 포함해야 합니다."),

    // 탈퇴
    ACCOUNT_DEACTIVATED(HttpStatus.BAD_REQUEST, "Bad Request", "비활성화된 계정입니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "존재하지 않은 상품입니다."),

    // 권한 관련 오류
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "Forbidden", "권한이 없는 접근입니다."),

    // 가격
    INVALID_BID_AMOUNT(HttpStatus.BAD_REQUEST, "Bad Request", "입찰 금액은 시작 입찰가보다 높아야 합니다."),
    INVALID_BID_AMOUNT_LOW(HttpStatus.BAD_REQUEST, "Bad Request", "입력된 가격이 현재 입찰가보다 낮습니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "유저를 찾을 수 없습니다."),

    // 입찰
    BID_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "입찰 정보를 찾을 수 없습니다."),
    INVALID_BID(HttpStatus.BAD_REQUEST, "Bad Request", "입찰 정보가 잘못되었습니다."),

    // 낙찰
    AWARD_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "낙찰된 상품을 찾을 수 없습니다."),

    // 채팅
    SENDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "발신자를 찾을 수 없습니다."),
    RECEIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "수신자를 찾을 수 없습니다."),
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "채팅 메시지를 찾을 수 없습니다."),

    // 거래
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found", "거래중이 아니거나 거래가 완료된 상품입니다."),

    // 이메일 변경
    EMAIL_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Bad Request", "이메일은 변경이 불가합니다.");

    private final HttpStatus status;
    private final String error;
    private final String message;
}