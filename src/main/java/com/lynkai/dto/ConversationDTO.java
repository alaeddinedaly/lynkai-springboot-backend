package com.lynkai.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ConversationDTO {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private Long userId;
    private LocalDateTime startedAt;
    private List<MessageDTO> messages;
    private Integer messageCount;
}