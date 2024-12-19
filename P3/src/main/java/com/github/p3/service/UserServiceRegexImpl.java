package com.github.p3.service;

public class UserServiceRegexImpl {
    // 이메일 형식 검증
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // 비밀번호 형식 검증 (8~20자, 소문자, 대문자, 숫자 중 2개 필수 조합)
    public static boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-zA-Z])(?=.*\\d)|(?=.*[a-z])(?=.*[A-Z])|(?=.*[A-Z])(?=.*\\d)|(?=.*[a-z])(?=.*\\d).{8,20}$";
        return password.matches(passwordRegex);
    }

    // 닉네임 형식 검증 (2~10자, 한글 또는 영문 또는 숫자 포함)
    public static boolean isValidNickname(String nickname) {
        String nicknameRegex = "^(?=.*[a-zA-Z가-힣0-9])[a-zA-Z가-힣0-9]{2,10}$";
        return nickname.matches(nicknameRegex);
    }
}
