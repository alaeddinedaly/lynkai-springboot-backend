package com.lynkai.repository;

import com.lynkai.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // Find a verification entry by userId and code
    Optional<EmailVerification> findByUserIdAndCode(Long userId, String code);

    // Delete a verification entry by userId and code (used after verification)
    void deleteByUserIdAndCode(Long userId, String code);

    // Optional: find by userId only (for resending code)
    Optional<EmailVerification> findByUserId(Long userId);

    // Optional: delete all codes for a user (e.g., when re-generating a new code)
    void deleteAllByUserId(Long userId);
}
