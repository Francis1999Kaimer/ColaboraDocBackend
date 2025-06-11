package com.example.project.controllers;

import com.example.project.DTO.VersionCreateRequestDTO;
import com.example.project.DTO.VersionDTO;
import com.example.project.annotation.RequiresPermission;
import com.example.project.entities.ProcessingStatus;
import com.example.project.entities.User;
import com.example.project.enums.Permission;
import com.example.project.repositories.UserRepository;
import com.example.project.services.DocumentProcessingService;
import com.example.project.services.DropboxService;
import com.example.project.services.VersionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class VersionController {

    private static final Logger logger = LoggerFactory.getLogger(VersionController.class);

    @Autowired
    private VersionService versionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentProcessingService processingService;

    @Autowired
    private DropboxService dropboxService;

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            logger.error("UserDetails es null en getCurrentUser del VersionController.");
            throw new RuntimeException("No se pudo determinar el usuario autenticado.");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    logger.error("Usuario autenticado con email '{}' no encontrado en la base de datos.", userDetails.getUsername());
                    return new RuntimeException("Usuario autenticado no encontrado en la base de datos.");
                });
    }   
    
    @PostMapping("/versions")
    @RequiresPermission(Permission.CREATE_VERSION)
    public ResponseEntity<VersionDTO> createVersion(
            @RequestParam("documentId") Integer documentId,
            @RequestParam("versionNumber") Integer versionNumber,
            @RequestParam(value = "comments", required = false) String comments,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User currentUser = getCurrentUser(userDetails);
            VersionDTO createdVersion = versionService.createVersionWithFile(documentId, versionNumber, comments, file, currentUser);
            return new ResponseEntity<>(createdVersion, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error al crear versión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }    
    
    @PostMapping("/versions/with-data")
    @RequiresPermission(Permission.CREATE_VERSION)
    public ResponseEntity<VersionDTO> createVersionWithData(
            @Valid @RequestBody VersionCreateRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = getCurrentUser(userDetails);
            VersionDTO createdVersion = versionService.createVersion(requestDTO, currentUser);
            return new ResponseEntity<>(createdVersion, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error al crear versión con datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }    @GetMapping("/documents/{documentId}/versions")
    public ResponseEntity<List<VersionDTO>> getVersionsByDocumentId(@PathVariable Integer documentId) {
        List<VersionDTO> versions = versionService.getVersionsForDocument(documentId);
        return ResponseEntity.ok(versions);
    }    
    
    @DeleteMapping("/delete/versions/{versionId}")
    @RequiresPermission(Permission.DELETE_VERSION)
    public ResponseEntity<String> deleteVersion(
            @PathVariable Integer versionId,
            @RequestParam(value = "deletedBy", required = false) String deletedBy) {
        try {
            versionService.deleteVersion(versionId, deletedBy);
            return ResponseEntity.ok("Versión eliminada exitosamente");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Versión no encontrada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PostMapping("/versions/{versionId}/restore")
    @RequiresPermission(Permission.RESTORE_VERSION)
    public ResponseEntity<String> restoreVersion(@PathVariable Integer versionId) {
        try {
            versionService.restoreVersion(versionId);
            return ResponseEntity.ok("Versión restaurada exitosamente");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Versión no encontrada");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }    
    
    @GetMapping("/documents/{documentId}/versions/deleted")
    public ResponseEntity<List<VersionDTO>> getDeletedVersionsByDocumentId(@PathVariable Integer documentId) {
        try {
            List<VersionDTO> deletedVersions = versionService.getDeletedVersionsForDocument(documentId);
            return ResponseEntity.ok(deletedVersions);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/versions/project/{projectId}/deleted")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<List<VersionDTO>> getDeletedVersionsByProject(@PathVariable Integer projectId) {
        try {
            List<VersionDTO> deletedVersions = versionService.getDeletedVersionsForProject(projectId);
            return ResponseEntity.ok(deletedVersions);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    

    @GetMapping("/versions/{versionId}/download")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<Resource> downloadVersion(@PathVariable Integer versionId) {
        try {
            VersionDTO version = versionService.getVersionById(versionId);
            
            
            byte[] fileContent = dropboxService.downloadFile(version.getDropboxFileId());
            
            
            String fileName = extractFileName(version.getDropboxFilePath());
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
                    
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error downloading version {}: {}", versionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    @GetMapping("/versions/{versionId}/view")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<Resource> viewVersion(@PathVariable Integer versionId) {
        try {
            VersionDTO version = versionService.getVersionById(versionId);
            
            
            ProcessingStatus status = processingService.getProcessingStatus(versionId);
            
            if (status == ProcessingStatus.PENDING || status == ProcessingStatus.PROCESSING) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .header("Processing-Status", status.toString())
                        .build();
            }
            
            if (status == ProcessingStatus.FAILED) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .header("Processing-Status", status.toString())
                        .build();
            }
            
            
            String fileId = version.getProcessedDropboxFileId() != null 
                    ? version.getProcessedDropboxFileId() 
                    : version.getDropboxFileId();
            
            byte[] fileContent = dropboxService.downloadFile(fileId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(fileContent.length)
                    .body(resource);
                    
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error viewing version {}: {}", versionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    @GetMapping("/versions/{versionId}/processing-status")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<Map<String, Object>> getProcessingStatus(@PathVariable Integer versionId) {
        try {
            ProcessingStatus status = processingService.getProcessingStatus(versionId);
            VersionDTO version = versionService.getVersionById(versionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", status);
            response.put("canView", status == ProcessingStatus.COMPLETED || status == ProcessingStatus.SKIPPED);
            response.put("hasProcessedFile", version.getProcessedDropboxFileId() != null);
            
            if (status == ProcessingStatus.FAILED) {
                response.put("errorMessage", version.getProcessingErrorMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting processing status for version {}: {}", versionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    @GetMapping("/versions/{versionId}/download-link")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<Map<String, String>> getDownloadLink(@PathVariable Integer versionId) {
        try {
            VersionDTO version = versionService.getVersionById(versionId);
            String downloadLink = dropboxService.getTemporaryDownloadLink(version.getDropboxFileId());
            
            Map<String, String> response = new HashMap<>();
            response.put("downloadLink", downloadLink);
            response.put("fileName", extractFileName(version.getDropboxFilePath()));
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting download link for version {}: {}", versionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    @PostMapping("/versions/{versionId}/process")
    @RequiresPermission(Permission.EDIT_VERSION)
    public ResponseEntity<Map<String, String>> processVersion(@PathVariable Integer versionId) {
        try {
            processingService.processDocumentAsync(versionId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Document processing started");
            response.put("status", "PROCESSING");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error starting processing for version {}: {}", versionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    private String extractFileName(String filePath) {
        if (filePath == null) return "document";
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
}