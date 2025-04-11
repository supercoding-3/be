package com.github.p3.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;

    // ErrorCode를 받는 생성자
    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value(); // .value()로 숫자 추출
        this.error = errorCode.getError();
        this.message = errorCode.getMessage();
    }

    // 직접 HttpStatus를 받는 생성자
    public ErrorResponse(HttpStatus status, String error, String message) {
        this.status = status.value(); // .value()로 숫자 추출
        this.error = error;
        this.message = message;
    }
}