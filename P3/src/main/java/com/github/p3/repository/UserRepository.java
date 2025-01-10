package com.github.p3.repository;

import com.github.p3.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE u.userEmail = :userEmail")
    Optional<User> findByUserEmail(String userEmail);
    Optional<User> findByUserNickname(String userNickname);
    Optional<User> findByUserId(Integer id);

}
