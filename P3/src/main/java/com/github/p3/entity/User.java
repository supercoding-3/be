package com.github.p3.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    @Column(name = "user_password", nullable = false)
    private String userPassword;

    @Column(name = "user_nickname", unique = true, nullable = false)
    private String userNickname;

    @Column(name = "user_phone", unique = true)
    private String userPhone;

    @Column(name = "user_created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at")
    @UpdateTimestamp
    private LocalDateTime userUpdatedAt;

    @Column(name = "user_is_deleted", nullable = false)
    private Boolean userIsDeleted = false;

}

