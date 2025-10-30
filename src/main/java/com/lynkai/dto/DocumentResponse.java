package com.lynkai.dto;

import com.lynkai.model.Document;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {
    private Long id;
    private String title;
    private String filePath;
    private String content;
    private Integer pageCount;
    private LocalDateTime createdAt;

    public static DocumentResponse fromEntity(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .filePath(document.getFilePath())
                .content(document.getContent())
                .createdAt(document.getCreatedAt())
                .pageCount(document.getPageCount())
                .build();
    }
}
