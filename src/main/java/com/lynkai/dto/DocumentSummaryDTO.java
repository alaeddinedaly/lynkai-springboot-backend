package com.lynkai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummaryDTO {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private LocalDateTime generatedAt;
    private String overview;
    private List<String> keyPoints;
    private List<String> mainTopics;
    private Integer wordCount;
    private Integer pageCount;
    private String summaryLength;
    private String summaryStyle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}