package com.github.p3.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDto {
    private String email;
    private String nickname;
    private String password;
    private String confirmPassword;
    private String phoneNumber;
}
