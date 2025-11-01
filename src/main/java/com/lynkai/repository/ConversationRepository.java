package com.lynkai.repository;

import com.lynkai.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Find all conversations for a specific user
    List<Conversation> findByUserIdOrderByStartedAtDesc(Long userId);

    // Find all conversations for a specific document
    List<Conversation> findByDocumentIdOrderByStartedAtDesc(Long documentId);

    // Find conversations by user and document
    List<Conversation> findByUserIdAndDocumentIdOrderByStartedAtDesc(Long userId, Long documentId);

    // Find the most recent conversation for a user and document
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId AND c.document.id = :documentId ORDER BY c.startedAt DESC")
    Optional<Conversation> findLatestByUserAndDocument(@Param("userId") Long userId, @Param("documentId") Long documentId);

    // Check if conversation exists
    boolean existsByIdAndUserId(Long conversationId, Long userId);
}