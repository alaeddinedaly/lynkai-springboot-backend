package com.lynkai.controller;

import com.lynkai.dto.DocumentResponse;
import com.lynkai.model.Document;
import com.lynkai.model.User;
import com.lynkai.service.DocumentService;
import com.lynkai.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;

    /**
     * Upload a document for the authenticated user.
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title
    ) throws IOException {
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserById(userId);

        DocumentResponse response = documentService.saveDocument(file, title, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all documents uploaded by the current user.
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getUserDocuments() {
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserById(userId);

        List<DocumentResponse> documents = documentService.getUserDocuments(user);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws IOException {
        DocumentResponse doc = documentService.getDocumentById(id);
        if (doc == null) return ResponseEntity.notFound().build();

        Path path = Paths.get(doc.getFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getTitle() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Path file = Paths.get("uploads").resolve(filename).normalize();
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());
        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }

    @PatchMapping("/{id}/rag")
    public ResponseEntity<DocumentResponse> updateRagInfo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        Integer ragDocumentId = updates.containsKey("ragDocumentId")
                ? (Integer) updates.get("ragDocumentId")
                : null;

        Boolean isProcessed = updates.containsKey("isProcessed")
                ? (Boolean) updates.get("isProcessed")
                : null;

        DocumentResponse updated = documentService.updateRagInfo(id, ragDocumentId, isProcessed);
        return ResponseEntity.ok(updated);
    }


}
