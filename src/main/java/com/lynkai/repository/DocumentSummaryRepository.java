package com.lynkai.repository;

import com.lynkai.dto.DocumentSummaryDTO;
import com.lynkai.model.DocumentSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentSummaryRepository extends JpaRepository<DocumentSummary, Long> {

    List<DocumentSummary> findByDocumentIdIn(List<Long> documentIds);

    // Find all summaries for a specific document (most recent first)
    List<DocumentSummary> getSummariesByDocumentIdIn(List<Long> documentIds);
    List<DocumentSummary> findByDocumentIdOrderByGeneratedAtDesc(Long documentId);

    // Find the most recent summary for a document
    Optional<DocumentSummary> findFirstByDocumentIdOrderByGeneratedAtDesc(Long documentId);

    // Check if summary exists for a document
    boolean existsByDocumentId(Long documentId);

    // Count summaries for a document
    long countByDocumentId(Long documentId);

    // Delete all summaries for a document
    void deleteByDocumentId(Long documentId);

    // Find summaries by style
    List<DocumentSummary> findBySummaryStyle(String summaryStyle);

    // Find summaries by length
    List<DocumentSummary> findBySummaryLength(String summaryLength);

    // Custom query to find summaries with pagination
    @Query("SELECT s FROM DocumentSummary s WHERE s.documentId = :documentId ORDER BY s.generatedAt DESC")
    List<DocumentSummary> findSummariesByDocument(@Param("documentId") Long documentId);
}