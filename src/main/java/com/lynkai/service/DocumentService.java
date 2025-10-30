package com.lynkai.service;

import com.lynkai.dto.DocumentResponse;
import com.lynkai.model.Document;
import com.lynkai.model.User;
import com.lynkai.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private int detectPageCount(Path filePath, String mimeType) {
        try {
            String fileName = filePath.getFileName().toString().toLowerCase();

            // 🧾 PDF
            if (fileName.endsWith(".pdf")) {
                try (var pdf = org.apache.pdfbox.pdmodel.PDDocument.load(filePath.toFile())) {
                    int pageCount = pdf.getNumberOfPages();
                    System.out.println("PDF detected: " + pageCount + " pages");
                    return pageCount;
                }
            }

            // 📝 DOCX
            if (fileName.endsWith(".docx")) {
                try (InputStream is = Files.newInputStream(filePath);
                     XWPFDocument docx = new XWPFDocument(is)) {

                    // Try to get page count from document properties
                    try {
                        int pages = docx.getProperties()
                                .getExtendedProperties()
                                .getUnderlyingProperties()
                                .getPages();

                        if (pages > 0) {
                            System.out.println("DOCX detected from properties: " + pages + " pages");
                            return pages;
                        }
                    } catch (Exception e) {
                        System.out.println("Could not read DOCX page count from properties: " + e.getMessage());
                    }

                    // Better estimation based on content
                    int totalParagraphs = docx.getParagraphs().size();
                    int totalTables = docx.getTables().size();

                    // Count actual text content (ignore empty paragraphs)
                    int contentParagraphs = 0;
                    for (var paragraph : docx.getParagraphs()) {
                        String text = paragraph.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            contentParagraphs++;
                        }
                    }

                    // Estimate: ~35-40 paragraphs per page, tables count as 5 paragraphs each
                    int contentUnits = contentParagraphs + (totalTables * 5);
                    int estimatedPages = Math.max(1, (int) Math.ceil(contentUnits / 35.0));

                    System.out.println("DOCX estimation - Total paragraphs: " + totalParagraphs +
                            ", Content paragraphs: " + contentParagraphs +
                            ", Tables: " + totalTables +
                            ", Estimated pages: " + estimatedPages);

                    return estimatedPages;
                }
            }

            // 📄 DOC (Word 97-2003)
            if (fileName.endsWith(".doc")) {
                try (InputStream is = Files.newInputStream(filePath);
                     POIFSFileSystem fs = new POIFSFileSystem(is);
                     HWPFDocument doc = new HWPFDocument(fs)) {

                    SummaryInformation info = doc.getSummaryInformation();
                    if (info != null) {
                        int pageCount = info.getPageCount();
                        if (pageCount > 0) {
                            System.out.println("DOC detected: " + pageCount + " pages");
                            return pageCount;
                        }
                    }

                    // Fallback
                    System.out.println("DOC page count not available, defaulting to 1");
                    return 1;
                }
            }

            // Default
            System.out.println("Unknown file type, defaulting to 1 page");
            return 1;

        } catch (Exception e) {
            System.err.println("Failed to detect page count for: " + filePath + " -> " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    public DocumentResponse saveDocument(MultipartFile file, String title, User user) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        // ✅ Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // ✅ Generate safe filename
        String safeFileName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(safeFileName);

        // ✅ Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // ✅ Detect file type and count pages
        int pageCount = detectPageCount(filePath, file.getContentType());
        System.out.println("Final page count for " + title + ": " + pageCount);

        // ✅ Build document
        Document document = Document.builder()
                .title(title)
                .filePath(filePath.toString())
                .pageCount(pageCount)
                .content(null)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        Document saved = documentRepository.save(document);

        // Verify it was saved
        System.out.println("Saved document with pageCount: " + saved.getPageCount());

        return DocumentResponse.fromEntity(saved);
    }

    public List<DocumentResponse> getUserDocuments(User user) {
        return documentRepository.findAllByUser(user)
                .stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public DocumentResponse getDocumentById(Long id) {
        Optional<Document> docOpt = documentRepository.findById(id);
        return docOpt.map(DocumentResponse::fromEntity).orElse(null);
    }

    // ✅ NEW: Update RAG document ID and processing status
    public DocumentResponse updateRagInfo(Long documentId, Integer ragDocumentId, Boolean isProcessed) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        if (ragDocumentId != null) {
            document.setRagDocumentId(ragDocumentId);
        }

        if (isProcessed != null) {
            document.setIsProcessed(isProcessed);
        }

        Document updated = documentRepository.save(document);
        return DocumentResponse.fromEntity(updated);
    }

    // ✅ Utility method to avoid dangerous characters in filenames
    private String sanitizeFilename(String original) {
        return original == null ? "unnamed" : original.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }
}