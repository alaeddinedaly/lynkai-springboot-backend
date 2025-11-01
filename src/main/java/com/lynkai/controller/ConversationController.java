package com.lynkai.controller;

import com.lynkai.dto.*;
import com.lynkai.service.ConversationService;
import com.lynkai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ConversationController {

    private final ConversationService conversationService;
    private final UserService userService;

    /**
     * Create a new conversation
     * POST /api/conversations
     */
    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody CreateConversationRequest request) {
        try {
            Long userId = userService.getCurrentUserId();
            ConversationDTO conversation = conversationService.createConversation(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get or create conversation for authenticated user and document
     * GET /api/conversations/document/{documentId}
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<ConversationDTO> getOrCreateConversation(@PathVariable Long documentId) {
        Long userId = userService.getCurrentUserId();
        try {
            ConversationDTO conversation = conversationService.getOrCreateConversation(userId, documentId);
            return ResponseEntity.ok(conversation);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get a specific conversation by ID
     * GET /api/conversations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable Long id) {
        try {
            Long userId = userService.getCurrentUserId();
            ConversationDTO conversation = conversationService.getConversation(id, userId);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get all conversations for the authenticated user
     * GET /api/conversations
     */
    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getUserConversations() {
        Long userId = userService.getCurrentUserId();
        List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get all conversations for a document (for the authenticated user)
     * GET /api/conversations/by-document/{documentId}
     */
    @GetMapping("/by-document/{documentId}")
    public ResponseEntity<List<ConversationDTO>> getDocumentConversations(@PathVariable Long documentId) {
        Long userId = userService.getCurrentUserId();
        List<ConversationDTO> conversations = conversationService.getDocumentConversations(documentId, userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Delete a conversation
     * DELETE /api/conversations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        try {
            Long userId = userService.getCurrentUserId();
            conversationService.deleteConversation(id, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Add a message to a conversation
     * POST /api/conversations/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> addMessage(@RequestBody SendMessageRequest request) {
        try {
            Long userId = userService.getCurrentUserId();
            MessageDTO message = conversationService.addMessage(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get all messages for a conversation
     * GET /api/conversations/{id}/messages
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(@PathVariable Long id) {
        try {
            Long userId = userService.getCurrentUserId();
            List<MessageDTO> messages = conversationService.getConversationMessages(id, userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}