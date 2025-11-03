package com.lynkai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentSummaryDTO {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotBlank(message = "Document title is required")
    private String documentTitle;

    private LocalDateTime generatedAt;

    @NotBlank(message = "Overview is required")
    private String overview;

    @NotNull(message = "Key points are required")
    private List<String> keyPoints;

    @NotNull(message = "Main topics are required")
    private List<String> mainTopics;

    private Integer wordCount;
    private Integer pageCount;
    private String summaryLength;
    private String summaryStyle;
}