package com.github.p3.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException ex) {
        log.error("CustomException 발생: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.error("예기치 못한 오류 발생: ", ex);
        return ResponseEntity
                .status(500)
                .body("서버에서 처리할 수 없는 오류가 발생했습니다.");
    }
}