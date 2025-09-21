package com.chatify.chat_backend.repository;

import com.chatify.chat_backend.entity.RefreshToken;
import com.chatify.chat_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    int deleteAllByUser(User user);

    Optional<RefreshToken> findByUser(User user);
}
