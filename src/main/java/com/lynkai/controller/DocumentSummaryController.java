package com.lynkai.controller;

import com.lynkai.dto.CreateDocumentSummaryDTO;
import com.lynkai.dto.DocumentSummaryDTO;
import com.lynkai.model.Document;
import com.lynkai.model.User;
import com.lynkai.repository.DocumentRepository;
import com.lynkai.service.DocumentSummaryService;
import com.lynkai.service.JwtService;
import com.lynkai.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
@Slf4j
public class DocumentSummaryController {

    private final DocumentSummaryService summaryService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user ID from JWT token
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        // The subject in JWT is the user ID (as String)
        String userIdStr = authentication.getName();

        try {
            Long userId = Long.parseLong(userIdStr);
            log.debug("Current authenticated user ID: {}", userId);
            return userId;
        } catch (NumberFormatException e) {
            log.error("Failed to parse user ID from token: {}", userIdStr);
            throw new RuntimeException("Invalid user ID in token");
        }
    }

    /**
     * Get the current user entity
     */
    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    /**
     * Verify that the current user owns the specified document
     */
    private void verifyDocumentOwnership(Long documentId) {
        Long currentUserId = getCurrentUserId();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        if (!document.getUser().getId().equals(currentUserId)) {
            log.warn("User {} attempted to access document {} owned by user {}",
                    currentUserId, documentId, document.getUser().getId());
            throw new SecurityException("Access denied: You don't have permission to access this document");
        }

        log.debug("Document ownership verified for user {} and document {}", currentUserId, documentId);
    }

    /**
     * Create a new document summary
     * POST /api/summaries
     */
    @PostMapping
    public ResponseEntity<DocumentSummaryDTO> createSummary(
            @Valid @RequestBody CreateDocumentSummaryDTO createDTO) {
        log.info("POST /api/summaries - Creating summary for document: {}", createDTO.getDocumentTitle());

        try {
            // Verify user owns the document before creating summary
            verifyDocumentOwnership(createDTO.getDocumentId());

            DocumentSummaryDTO createdSummary = summaryService.createSummary(createDTO);
            log.info("Successfully created summary with ID: {}", createdSummary.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSummary);
        } catch (SecurityException e) {
            log.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error creating summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all summaries for the current user's documents
     * GET /api/summaries
     */
    @GetMapping
    public ResponseEntity<List<DocumentSummaryDTO>> getAllSummaries() {
        log.info("GET /api/summaries - Fetching all summaries for current user");

        try {
            Long currentUserId = getCurrentUserId();

            // Get all documents owned by current user
            List<Document> userDocuments = documentRepository.findAllByUserId(currentUserId);

            if (userDocuments.isEmpty()) {
                log.info("No documents found for user {}", currentUserId);
                return ResponseEntity.ok(List.of());
            }

            // Get all document IDs
            List<Long> documentIds = userDocuments.stream()
                    .map(Document::getId)
                    .toList();

            // Get all summaries for those documents
            List<DocumentSummaryDTO> summaries = summaryService.getSummariesByDocumentIds(documentIds);

            log.info("Found {} summaries for user {}", summaries.size(), currentUserId);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            log.error("Error fetching summaries: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get summary by ID
     * GET /api/summaries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentSummaryDTO> getSummaryById(@PathVariable Long id) {
        log.info("GET /api/summaries/{} - Fetching summary", id);

        try {
            DocumentSummaryDTO summary = summaryService.getSummaryById(id);

            // Verify user owns the document this summary belongs to
            verifyDocumentOwnership(summary.getDocumentId());

            return ResponseEntity.ok(summary);
        } catch (SecurityException e) {
            log.error("Access denied for summary {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.error("Error fetching summary {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all summaries for a specific document
     * GET /api/summaries/document/{documentId}
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<DocumentSummaryDTO>> getSummariesByDocumentId(
            @PathVariable Long documentId) {
        log.info("GET /api/summaries/document/{} - Fetching summaries for document", documentId);

        try {
            // Verify user owns the document
            verifyDocumentOwnership(documentId);

            List<DocumentSummaryDTO> summaries = summaryService.getSummariesByDocumentId(documentId);
            log.info("Found {} summaries for document {}", summaries.size(), documentId);
            return ResponseEntity.ok(summaries);
        } catch (SecurityException e) {
            log.error("Access denied for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.error("Error fetching summaries for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get the latest summary for a document
     * GET /api/summaries/document/{documentId}/latest
     */
    @GetMapping("/document/{documentId}/latest")
    public ResponseEntity<DocumentSummaryDTO> getLatestSummaryByDocumentId(
            @PathVariable Long documentId) {
        log.info("GET /api/summaries/document/{}/latest - Fetching latest summary", documentId);

        try {
            // Verify user owns the document
            verifyDocumentOwnership(documentId);

            DocumentSummaryDTO summary = summaryService.getLatestSummaryByDocumentId(documentId);
            return ResponseEntity.ok(summary);
        } catch (SecurityException e) {
            log.error("Access denied for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.error("No summary found for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update a summary
     * PUT /api/summaries/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentSummaryDTO> updateSummary(
            @PathVariable Long id,
            @Valid @RequestBody CreateDocumentSummaryDTO updateDTO) {
        log.info("PUT /api/summaries/{} - Updating summary", id);

        try {
            // Get existing summary to verify ownership
            DocumentSummaryDTO existingSummary = summaryService.getSummaryById(id);
            verifyDocumentOwnership(existingSummary.getDocumentId());

            // Also verify ownership of new document if it's being changed
            if (!existingSummary.getDocumentId().equals(updateDTO.getDocumentId())) {
                verifyDocumentOwnership(updateDTO.getDocumentId());
            }

            DocumentSummaryDTO updatedSummary = summaryService.updateSummary(id, updateDTO);
            log.info("Successfully updated summary {}", id);
            return ResponseEntity.ok(updatedSummary);
        } catch (SecurityException e) {
            log.error("Access denied for summary {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.error("Error updating summary {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a summary
     * DELETE /api/summaries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSummary(@PathVariable Long id) {
        log.info("DELETE /api/summaries/{} - Deleting summary", id);

        try {
            // Get summary to verify ownership before deleting
            DocumentSummaryDTO summary = summaryService.getSummaryById(id);
            verifyDocumentOwnership(summary.getDocumentId());

            summaryService.deleteSummary(id);
            log.info("Successfully deleted summary {}", id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            log.error("Access denied for summary {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            log.error("Error deleting summary {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete all summaries for a document
     * DELETE /api/summaries/document/{documentId}
     */
    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<Void> deleteSummariesByDocumentId(@PathVariable Long documentId) {
        log.info("DELETE /api/summaries/document/{} - Deleting all summaries for document", documentId);

        try {
            // Verify user owns the document
            verifyDocumentOwnership(documentId);

            summaryService.deleteSummariesByDocumentId(documentId);
            log.info("Successfully deleted all summaries for document {}", documentId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            log.error("Access denied for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error deleting summaries for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check if summary exists for a document
     * GET /api/summaries/document/{documentId}/exists
     */
    @GetMapping("/document/{documentId}/exists")
    public ResponseEntity<Boolean> summaryExistsForDocument(@PathVariable Long documentId) {
        log.info("GET /api/summaries/document/{}/exists - Checking if summary exists", documentId);

        try {
            // Verify user owns the document
            verifyDocumentOwnership(documentId);

            boolean exists = summaryService.summaryExistsForDocument(documentId);
            return ResponseEntity.ok(exists);
        } catch (SecurityException e) {
            log.error("Access denied for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error checking summary existence for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Get summary count for a document
     * GET /api/summaries/document/{documentId}/count
     */
    @GetMapping("/document/{documentId}/count")
    public ResponseEntity<Long> getSummaryCountByDocumentId(@PathVariable Long documentId) {
        log.info("GET /api/summaries/document/{}/count - Getting summary count", documentId);

        try {
            // Verify user owns the document
            verifyDocumentOwnership(documentId);

            long count = summaryService.getSummaryCountByDocumentId(documentId);
            return ResponseEntity.ok(count);
        } catch (SecurityException e) {
            log.error("Access denied for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error getting summary count for document {}: {}", documentId, e.getMessage());
            return ResponseEntity.ok(0L);
        }
    }
}