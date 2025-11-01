package com.lynkai.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private String sender; // "user" or "ai"
    private String content;
    private LocalDateTime timestamp;
    private Long conversationId;
}