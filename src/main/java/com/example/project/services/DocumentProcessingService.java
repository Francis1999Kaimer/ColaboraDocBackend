package com.example.project.services;

import com.example.project.entities.ProcessingStatus;
import com.example.project.entities.Version;
import com.example.project.repositories.VersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private DropboxService dropboxService;

    

    @Async
    @Transactional
    public CompletableFuture<Void> processDocumentAsync(Integer versionId) {
        try {
            Version version = versionRepository.findById(versionId)
                    .orElseThrow(() -> new RuntimeException("Version not found: " + versionId));

            
            version.setProcessingStatus(ProcessingStatus.PROCESSING);
            versionRepository.save(version);

            logger.info("Starting document processing for version: {}", versionId);

            
            if (isPdfDocument(version.getMimeType())) {
                handlePdfDocument(version);
            } else if (isProcessableDocument(version.getMimeType())) {
                processToPdf(version);
            } else {
                skipProcessing(version);
            }

            logger.info("Document processing completed for version: {}", versionId);

        } catch (Exception e) {
            logger.error("Error processing document for version: {}", versionId, e);
            handleProcessingError(versionId, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }
    

    

    private boolean isPdfDocument(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    

    private boolean isProcessableDocument(String mimeType) {
        return mimeType != null && (
                mimeType.startsWith("application/vnd.openxmlformats-officedocument") || 
                mimeType.startsWith("application/vnd.ms-") || 
                mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.ms-powerpoint")
        );
    }

    

    @Transactional
    private void handlePdfDocument(Version version) {
        try {
            
            version.setProcessedDropboxFileId(version.getDropboxFileId());
            version.setProcessedDropboxFilePath(version.getDropboxFilePath());
            version.setProcessingStatus(ProcessingStatus.SKIPPED);
            versionRepository.save(version);

            logger.info("PDF document skipped processing for version: {}", version.getIdversion());

        } catch (Exception e) {
            logger.error("Error handling PDF document for version: {}", version.getIdversion(), e);
            throw new RuntimeException("Failed to handle PDF document", e);
        }
    }

    

    @Transactional
    private void processToPdf(Version version) {
        try {
            
            byte[] originalContent = dropboxService.downloadFile(version.getDropboxFileId());
            
            
            Path tempDir = Files.createTempDirectory("doc-processing-");
            String originalFileName = extractFileName(version.getDropboxFilePath());
            Path originalFile = tempDir.resolve(originalFileName);
            Files.write(originalFile, originalContent);

            
            Path pdfFile = tempDir.resolve(changeExtensionToPdf(originalFileName));
            convertToPdf(originalFile, pdfFile, version.getMimeType());

            
            String processedFileName = generateProcessedFileName(version, "pdf");
            
            
            byte[] pdfContent = Files.readAllBytes(pdfFile);
            String processedFileId = dropboxService.uploadFile(pdfContent, processedFileName);

            
            version.setProcessedDropboxFileId(processedFileId);
            version.setProcessedDropboxFilePath(processedFileName);
            version.setProcessingStatus(ProcessingStatus.COMPLETED);
            versionRepository.save(version);

            
            Files.deleteIfExists(originalFile);
            Files.deleteIfExists(pdfFile);
            Files.deleteIfExists(tempDir);

            logger.info("Document successfully converted to PDF for version: {}", version.getIdversion());

        } catch (Exception e) {
            logger.error("Error converting document to PDF for version: {}", version.getIdversion(), e);
            throw new RuntimeException("Failed to convert document to PDF", e);
        }
    }

    

    @Transactional
    private void skipProcessing(Version version) {
        version.setProcessingStatus(ProcessingStatus.SKIPPED);
        version.setProcessingErrorMessage("Document type not supported for processing");
        versionRepository.save(version);

        logger.info("Processing skipped for unsupported document type: {} for version: {}", 
                   version.getMimeType(), version.getIdversion());
    }

    

    @Transactional
    private void handleProcessingError(Integer versionId, String errorMessage) {
        try {
            Version version = versionRepository.findById(versionId).orElse(null);
            if (version != null) {
                version.setProcessingStatus(ProcessingStatus.FAILED);
                version.setProcessingErrorMessage(errorMessage);
                versionRepository.save(version);
            }
        } catch (Exception e) {
            logger.error("Error updating processing status for version: {}", versionId, e);
        }
    }

    

    private void convertToPdf(Path inputFile, Path outputFile, String mimeType) throws IOException, InterruptedException {
        
        
        
        ProcessBuilder processBuilder = new ProcessBuilder(
                "libreoffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputFile.getParent().toString(),
                inputFile.toString()
        );
        
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RuntimeException("LibreOffice conversion failed with exit code: " + exitCode);
        }

        
        Path generatedPdf = outputFile.getParent().resolve(
                inputFile.getFileName().toString().replaceAll("\\.[^.]+$", ".pdf")
        );
        
        if (!Files.exists(generatedPdf)) {
            throw new RuntimeException("PDF file was not generated");
        }

        
        if (!generatedPdf.equals(outputFile)) {
            Files.move(generatedPdf, outputFile);
        }
    }

    

    private String generateProcessedFileName(Version version, String extension) {
        try {
            String originalPath = version.getDropboxFilePath();
            String originalFileName = extractFileName(originalPath);
            String baseName = originalFileName.replaceAll("\\.[^.]+$", "");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            
            String directory = originalPath.substring(0, originalPath.lastIndexOf('/') + 1);
            return directory + "processed_v" + version.getVersionNumber() + "_" + timestamp + "_" + baseName + "." + extension;
            
        } catch (Exception e) {
            
            return "processed_v" + version.getVersionNumber() + "_" + System.currentTimeMillis() + "." + extension;
        }
    }

    

    private String extractFileName(String filePath) {
        if (filePath == null) return "document";
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    

    private String changeExtensionToPdf(String fileName) {
        return fileName.replaceAll("\\.[^.]+$", ".pdf");
    }

    

    @Transactional(readOnly = true)
    public ProcessingStatus getProcessingStatus(Integer versionId) {
        return versionRepository.findById(versionId)
                .map(Version::getProcessingStatus)
                .orElse(ProcessingStatus.FAILED);
    }
}
