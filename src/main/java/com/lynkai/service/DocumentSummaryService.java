package com.lynkai.service;

import com.lynkai.dto.CreateDocumentSummaryDTO;
import com.lynkai.dto.DocumentSummaryDTO;
import com.lynkai.model.DocumentSummary;
import com.lynkai.repository.DocumentSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSummaryService {

    private final DocumentSummaryRepository summaryRepository;

    /**
     * Create a new document summary
     */
    @Transactional
    public DocumentSummaryDTO createSummary(CreateDocumentSummaryDTO createDTO) {
        log.info("Creating summary for document ID: {}", createDTO.getDocumentId());

        DocumentSummary summary = new DocumentSummary();
        summary.setDocumentId(createDTO.getDocumentId());
        summary.setDocumentTitle(createDTO.getDocumentTitle());
        summary.setGeneratedAt(createDTO.getGeneratedAt() != null ? createDTO.getGeneratedAt() : LocalDateTime.now());
        summary.setOverview(createDTO.getOverview());
        summary.setKeyPoints(createDTO.getKeyPoints());
        summary.setMainTopics(createDTO.getMainTopics());
        summary.setWordCount(createDTO.getWordCount());
        summary.setPageCount(createDTO.getPageCount());
        summary.setSummaryLength(createDTO.getSummaryLength());
        summary.setSummaryStyle(createDTO.getSummaryStyle());

        DocumentSummary savedSummary = summaryRepository.save(summary);
        log.info("Successfully created summary with ID: {}", savedSummary.getId());

        return convertToDTO(savedSummary);
    }

    /**
     * Get summary by ID
     */
    @Transactional(readOnly = true)
    public DocumentSummaryDTO getSummaryById(Long id) {
        log.info("Fetching summary with ID: {}", id);

        DocumentSummary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Summary not found with ID: " + id));

        return convertToDTO(summary);
    }

    /**
     * Get all summaries
     */
    @Transactional(readOnly = true)
    public List<DocumentSummaryDTO> getAllSummaries() {
        log.info("Fetching all summaries");

        return summaryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all summaries for a specific document
     */
    @Transactional(readOnly = true)
    public List<DocumentSummaryDTO> getSummariesByDocumentId(Long documentId) {
        log.info("Fetching summaries for document ID: {}", documentId);

        return summaryRepository.findByDocumentIdOrderByGeneratedAtDesc(documentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get the most recent summary for a document
     */
    @Transactional(readOnly = true)
    public DocumentSummaryDTO getLatestSummaryByDocumentId(Long documentId) {
        log.info("Fetching latest summary for document ID: {}", documentId);

        DocumentSummary summary = summaryRepository.findFirstByDocumentIdOrderByGeneratedAtDesc(documentId)
                .orElseThrow(() -> new RuntimeException("No summary found for document ID: " + documentId));

        return convertToDTO(summary);
    }

    /**
     * Update an existing summary
     */
    @Transactional
    public DocumentSummaryDTO updateSummary(Long id, CreateDocumentSummaryDTO updateDTO) {
        log.info("Updating summary with ID: {}", id);

        DocumentSummary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Summary not found with ID: " + id));

        summary.setDocumentTitle(updateDTO.getDocumentTitle());
        summary.setOverview(updateDTO.getOverview());
        summary.setKeyPoints(updateDTO.getKeyPoints());
        summary.setMainTopics(updateDTO.getMainTopics());
        summary.setWordCount(updateDTO.getWordCount());
        summary.setPageCount(updateDTO.getPageCount());
        summary.setSummaryLength(updateDTO.getSummaryLength());
        summary.setSummaryStyle(updateDTO.getSummaryStyle());

        DocumentSummary updatedSummary = summaryRepository.save(summary);
        log.info("Successfully updated summary with ID: {}", id);

        return convertToDTO(updatedSummary);
    }

    /**
     * Delete a summary
     */
    @Transactional
    public void deleteSummary(Long id) {
        log.info("Deleting summary with ID: {}", id);

        if (!summaryRepository.existsById(id)) {
            throw new RuntimeException("Summary not found with ID: " + id);
        }

        summaryRepository.deleteById(id);
        log.info("Successfully deleted summary with ID: {}", id);
    }

    /**
     * Delete all summaries for a document
     */
    @Transactional
    public void deleteSummariesByDocumentId(Long documentId) {
        log.info("Deleting all summaries for document ID: {}", documentId);

        summaryRepository.deleteByDocumentId(documentId);
        log.info("Successfully deleted all summaries for document ID: {}", documentId);
    }

    /**
     * Check if summary exists for a document
     */
    @Transactional(readOnly = true)
    public boolean summaryExistsForDocument(Long documentId) {
        return summaryRepository.existsByDocumentId(documentId);
    }

    /**
     * Get summary count for a document
     */
    @Transactional(readOnly = true)
    public long getSummaryCountByDocumentId(Long documentId) {
        return summaryRepository.countByDocumentId(documentId);
    }

    /**
     * Convert entity to DTO
     */
    private DocumentSummaryDTO convertToDTO(DocumentSummary summary) {
        DocumentSummaryDTO dto = new DocumentSummaryDTO();
        dto.setId(summary.getId());
        dto.setDocumentId(summary.getDocumentId());
        dto.setDocumentTitle(summary.getDocumentTitle());
        dto.setGeneratedAt(summary.getGeneratedAt());
        dto.setOverview(summary.getOverview());
        dto.setKeyPoints(summary.getKeyPoints());
        dto.setMainTopics(summary.getMainTopics());
        dto.setWordCount(summary.getWordCount());
        dto.setPageCount(summary.getPageCount());
        dto.setSummaryLength(summary.getSummaryLength());
        dto.setSummaryStyle(summary.getSummaryStyle());
        dto.setCreatedAt(summary.getCreatedAt());
        dto.setUpdatedAt(summary.getUpdatedAt());
        return dto;
    }



    public List<DocumentSummaryDTO> getSummariesByDocumentIds(List<Long> documentIds) {
        log.info("Fetching summaries for {} documents", documentIds.size());

        if (documentIds.isEmpty()) {
            return List.of();
        }

        List<DocumentSummary> summaries = summaryRepository.findByDocumentIdIn(documentIds);

        log.info("Found {} summaries for {} documents", summaries.size(), documentIds.size());

        return summaries.stream()
                .map(this::convertToDTO)
                .toList();
    }
}