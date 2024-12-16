package com.github.p3.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String userEmail;
    private String userPassword;
    private String userPasswordConfirm;
    private String userNickname;
    private String userPhone;
    private Boolean userIsDeleted;

}
