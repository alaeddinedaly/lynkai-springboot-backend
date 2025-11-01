package com.lynkai.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    private Long conversationId;
    private String sender; // "user" or "ai"
    private String content;
}