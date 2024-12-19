package com.github.p3.exceptions;

public enum ErrorCode {
    EMPTY_FILE_EXCEPTION("파일이 비어있습니다."),
    IO_EXCEPTION_ON_IMAGE_UPLOAD("이미지 업로드 중 I/O 예외가 발생했습니다."),
    NO_FILE_EXTENTION("파일 확장자가 없습니다."),
    INVALID_FILE_EXTENTION("허용되지 않는 파일 확장자입니다."),
    PUT_OBJECT_EXCEPTION("S3에 파일을 업로드하는 중 오류가 발생했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
