package com.example.project.controllers;

import com.example.project.DTO.FolderCreateRequestDTO;
import com.example.project.DTO.FolderDTO;
import com.example.project.annotation.RequiresPermission;
import com.example.project.enums.Permission;
import com.example.project.services.FolderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class FolderController {

    @Autowired
    private FolderService folderService;    
    @PostMapping
    @RequiresPermission(Permission.CREATE_FOLDER)
    public ResponseEntity<FolderDTO> createFolder(@Valid @RequestBody FolderCreateRequestDTO requestDTO) {
        FolderDTO createdFolder = folderService.createFolder(requestDTO);
        return new ResponseEntity<>(createdFolder, HttpStatus.CREATED);
    }
    
    @DeleteMapping("/delete/{folderId}")
    @RequiresPermission(Permission.DELETE_FOLDER)
    public ResponseEntity<String> deleteFolder(
            @PathVariable Integer folderId,
            @RequestParam(value = "deletedBy", required = false) String deletedBy) {
        try {
            folderService.deleteFolder(folderId, deletedBy);
            return ResponseEntity.ok("Carpeta y contenidos eliminados exitosamente");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PostMapping("/{folderId}/restore")
    @RequiresPermission(Permission.DELETE_FOLDER)
    public ResponseEntity<String> restoreFolder(
            @PathVariable Integer folderId,
            @RequestParam(value = "newParentFolderId", required = false) Integer newParentFolderId) {
        try {
            folderService.restoreFolder(folderId, newParentFolderId);
            String message = newParentFolderId != null ? 
                "Carpeta restaurada exitosamente con nuevo padre" : 
                "Carpeta restaurada exitosamente";
            return ResponseEntity.ok(message);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @PostMapping("/{folderId}/restore-cascade")
    @RequiresPermission(Permission.DELETE_FOLDER)
    public ResponseEntity<String> restoreFolderCascade(
            @PathVariable Integer folderId,
            @RequestParam(value = "newParentFolderId", required = false) Integer newParentFolderId) {
        try {
            folderService.restoreFolderCascade(folderId, newParentFolderId);
            String message = newParentFolderId != null ? 
                "Carpeta y contenidos restaurados exitosamente con nuevo padre" : 
                "Carpeta y contenidos restaurados exitosamente";
            return ResponseEntity.ok(message);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }
    
    @GetMapping("/project/{projectId}/deleted")
    public ResponseEntity<List<FolderDTO>> getDeletedFoldersByProject(@PathVariable Integer projectId) {
        try {
            List<FolderDTO> deletedFolders = folderService.getDeletedFoldersByProjectWithHierarchy(projectId);
            return ResponseEntity.ok(deletedFolders);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{parentFolderId}/deleted")
    public ResponseEntity<List<FolderDTO>> getDeletedFoldersByParent(@PathVariable Integer parentFolderId) {
        try {
            List<FolderDTO> deletedFolders = folderService.getDeletedFoldersByParent(parentFolderId);
            return ResponseEntity.ok(deletedFolders);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}