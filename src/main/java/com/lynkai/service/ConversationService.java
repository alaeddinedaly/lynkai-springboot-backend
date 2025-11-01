package com.lynkai.service;

import com.lynkai.dto.*;
import com.lynkai.model.Conversation;
import com.lynkai.model.Document;
import com.lynkai.model.Message;
import com.lynkai.model.User;
import com.lynkai.repository.ConversationRepository;
import com.lynkai.repository.DocumentRepository;
import com.lynkai.repository.MessageRepository;
import com.lynkai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    /**
     * Create a new conversation
     */
    @Transactional
    public ConversationDTO createConversation(CreateConversationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Verify that the document belongs to the user
        if (!Objects.equals(document.getUser().getId(), userId)) {
            throw new RuntimeException("Unauthorized: Document does not belong to user");
        }

        Conversation conversation = Conversation.builder()
                .user(user)
                .document(document)
                .build();

        conversation = conversationRepository.save(conversation);

        return convertToDTO(conversation, true);
    }

    /**
     * Get or create conversation for user and document
     * If a conversation already exists, return it; otherwise create new one
     */
    @Transactional
    public ConversationDTO getOrCreateConversation(Long userId, Long documentId) {
        // Verify document belongs to user
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        log.info("Current userId: {}", userId);
        log.info("Document userId: {}", document.getUser().getId());

        if (!Objects.equals(document.getUser().getId(), userId)) {
            throw new RuntimeException("Unauthorized: Document does not belong to user");
        }

        return conversationRepository.findLatestByUserAndDocument(userId, documentId)
                .map(conv -> convertToDTO(conv, true))
                .orElseGet(() -> {
                    CreateConversationRequest request = new CreateConversationRequest();
                    request.setDocumentId(documentId);
                    return createConversation(request, userId);
                });
    }

    /**
     * Get conversation by ID with user validation
     */
    @Transactional(readOnly = true)
    public ConversationDTO getConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Verify conversation belongs to user
        if (!Objects.equals(conversation.getUser().getId(), userId)) {
            throw new RuntimeException("Unauthorized: Conversation does not belong to user");
        }

        return convertToDTO(conversation, true);
    }

    /**
     * Get all conversations for a user
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(conv -> convertToDTO(conv, false))
                .collect(Collectors.toList());
    }

    /**
     * Get all conversations for a document (for a specific user)
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getDocumentConversations(Long documentId, Long userId) {
        // Verify document belongs to user
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!Objects.equals(document.getUser().getId(), userId)) {
            throw new RuntimeException("Unauthorized: Document does not belong to user");
        }

        return conversationRepository.findByUserIdAndDocumentIdOrderByStartedAtDesc(userId, documentId)
                .stream()
                .map(conv -> convertToDTO(conv, false))
                .collect(Collectors.toList());
    }

    /**
     * Get conversations by user and document
     */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserDocumentConversations(Long userId, Long documentId) {
        return conversationRepository.findByUserIdAndDocumentIdOrderByStartedAtDesc(userId, documentId)
                .stream()
                .map(conv -> convertToDTO(conv, false))
                .collect(Collectors.toList());
    }

    /**
     * Delete a conversation with user validation
     */
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        if (!conversationRepository.existsByIdAndUserId(conversationId, userId)) {
            throw new RuntimeException("Conversation not found or unauthorized");
        }
        conversationRepository.deleteById(conversationId);
    }

    /**
     * Add a message to a conversation with user validation
     */
    @Transactional
    public MessageDTO addMessage(SendMessageRequest request, Long userId) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Verify conversation belongs to user
        if (!Objects.equals(conversation.getUser().getId(), userId)) {
            throw new RuntimeException("Unauthorized: Conversation does not belong to user");
        }

        Message message = Message.builder()
                .sender(request.getSender())
                .content(request.getContent())
                .conversation(conversation)
                .build();

        message = messageRepository.save(message);

        return convertMessageToDTO(message);
    }

    /**
     * Get all messages for a conversation with user validation
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getConversationMessages(Long conversationId, Long userId) {
        // Verify conversation belongs to user
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!Objects.equals(conversation.getUser().getId(), userId)) {
            throw new RuntimeException("Unauthorized: Conversation does not belong to user");
        }

        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId)
                .stream()
                .map(this::convertMessageToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get messages without user validation (internal use)
     */
    @Transactional(readOnly = true)
    private List<MessageDTO> getConversationMessagesInternal(Long conversationId) {
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId)
                .stream()
                .map(this::convertMessageToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Conversation entity to DTO
     */
    private ConversationDTO convertToDTO(Conversation conversation, boolean includeMessages) {
        ConversationDTO dto = ConversationDTO.builder()
                .id(conversation.getId())
                .documentId(conversation.getDocument().getId())
                .documentTitle(conversation.getDocument().getTitle())
                .userId(conversation.getUser().getId())
                .startedAt(conversation.getStartedAt())
                .messageCount((int) messageRepository.countByConversationId(conversation.getId()))
                .build();

        if (includeMessages) {
            dto.setMessages(getConversationMessagesInternal(conversation.getId()));
        }

        return dto;
    }

    /**
     * Convert Message entity to DTO
     */
    private MessageDTO convertMessageToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .sender(message.getSender())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .conversationId(message.getConversation().getId())
                .build();
    }
}