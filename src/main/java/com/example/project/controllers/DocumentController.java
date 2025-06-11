package com.example.project.controllers;

import com.example.project.DTO.DocumentCreateRequestDTO;
import com.example.project.DTO.DocumentDTO;
import com.example.project.DTO.DocumentWithFolderDTO;
import com.example.project.annotation.RequiresPermission;
import com.example.project.enums.Permission;
import com.example.project.services.DocumentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class DocumentController {

    @Autowired
    private DocumentService documentService;    @PostMapping
    @RequiresPermission(Permission.CREATE_DOCUMENT)
    public ResponseEntity<DocumentDTO> createDocument(@Valid @RequestBody DocumentCreateRequestDTO requestDTO) {
        DocumentDTO createdDocument = documentService.createDocument(requestDTO);
        return new ResponseEntity<>(createdDocument, HttpStatus.CREATED);
    }
    
      @DeleteMapping("/delete/{documentId}")
    @RequiresPermission(Permission.DELETE_DOCUMENT)
    public ResponseEntity<String> deleteDocument(
            @PathVariable Integer documentId,
            @RequestParam(value = "deletedBy", required = false) String deletedBy) {
        try {
            documentService.deleteDocument(documentId, deletedBy);
            return ResponseEntity.ok("Documento y versiones eliminados exitosamente");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }    @PostMapping("/{documentId}/restore")
    @RequiresPermission(Permission.DELETE_DOCUMENT)
    public ResponseEntity<String> restoreDocument(@PathVariable Integer documentId) {
        try {
            documentService.restoreDocument(documentId);
            return ResponseEntity.ok("Documento y versiones restaurados exitosamente");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @PostMapping("/{documentId}/restore-cascade")
    @RequiresPermission(Permission.DELETE_DOCUMENT)
    public ResponseEntity<String> restoreDocumentCascade(@PathVariable Integer documentId) {
        try {
            documentService.restoreDocumentCascade(documentId);
            return ResponseEntity.ok("Documento y versiones restaurados exitosamente en cascada");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Documento no encontrado");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @GetMapping("/folder/{folderId}/deleted")
    public ResponseEntity<List<DocumentDTO>> getDeletedDocumentsByFolder(@PathVariable Integer folderId) {
        try {
            List<DocumentDTO> deletedDocuments = documentService.getDeletedDocumentsByFolderId(folderId);
            return ResponseEntity.ok(deletedDocuments);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }    
    
    @GetMapping("/project/{projectId}/deleted")
    @RequiresPermission(Permission.VIEW_DOCUMENT)
    public ResponseEntity<List<DocumentWithFolderDTO>> getDeletedDocumentsByProject(@PathVariable Integer projectId) {
        try {
            List<DocumentWithFolderDTO> deletedDocuments = documentService.getDeletedDocumentsByProject(projectId);
            return ResponseEntity.ok(deletedDocuments);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}