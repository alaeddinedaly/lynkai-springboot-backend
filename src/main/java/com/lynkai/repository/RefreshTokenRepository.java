package com.lynkai.repository;

import com.lynkai.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserIdAndHashedToken(Long userId, String hashedToken);

    void deleteByUserIdAndHashedToken(Long userId, String hashedToken);

    void deleteAllByUserId(Long userId);

    void deleteByHashedToken(String hashedToken);
}
