package com.chatify.chat_backend.repository;

import com.chatify.chat_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    // For LOGIN: Find a user by their email to verify password
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // For REGISTRATION: Check if email is already taken (efficient exists check)
    Boolean existsByEmail(String email);

    // For REGISTRATION: Check if username is already taken (if username must be unique)
    Boolean existsByUsername(String username);

}

