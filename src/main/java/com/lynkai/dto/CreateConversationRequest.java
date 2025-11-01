package com.lynkai.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {
    private Long documentId;
    // userId removed - extracted from JWT token in backend
}