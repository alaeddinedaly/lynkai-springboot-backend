package com.lynkai.repository;

import com.lynkai.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find all messages for a conversation, ordered by timestamp
    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);

    // Find messages by conversation and sender
    List<Message> findByConversationIdAndSenderOrderByTimestampAsc(Long conversationId, String sender);

    // Count messages in a conversation
    long countByConversationId(Long conversationId);

    // Delete all messages for a conversation
    void deleteByConversationId(Long conversationId);
}