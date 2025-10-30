package com.lynkai.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentUploadRequest {
    private String title;
    private MultipartFile file;
}
