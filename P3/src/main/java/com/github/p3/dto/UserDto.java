package com.github.p3.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Integer userId;
    private String userEmail;
    private String userPassword;
    private String userNickname;
    private String userPhone;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
    private Boolean userIsDeleted;
    private String profileImageUrl;

}
