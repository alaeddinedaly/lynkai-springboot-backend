package com.lynkai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "document_title", nullable = false, length = 500)
    private String documentTitle;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "overview", columnDefinition = "TEXT", nullable = false)
    private String overview;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "summary_key_points", joinColumns = @JoinColumn(name = "summary_id"))
    @Column(name = "key_point", columnDefinition = "TEXT")
    @OrderColumn(name = "point_order")
    private List<String> keyPoints = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "summary_main_topics", joinColumns = @JoinColumn(name = "summary_id"))
    @Column(name = "topic", length = 255)
    @OrderColumn(name = "topic_order")
    private List<String> mainTopics = new ArrayList<>();

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "summary_length", length = 20)
    private String summaryLength; // short, medium, detailed

    @Column(name = "summary_style", length = 20)
    private String summaryStyle; // bullet, paragraph, executive

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}