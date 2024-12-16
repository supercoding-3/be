package com.github.p3.repository;

import com.github.p3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserEmail(String userEmail);
    Optional<User> findByUserNickname(String userNickname);

}
