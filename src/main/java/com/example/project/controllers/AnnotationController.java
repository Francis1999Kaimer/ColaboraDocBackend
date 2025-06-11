package com.example.project.controllers;

import com.example.project.DTO.AnnotationDTO;
import com.example.project.entities.AnnotationType;
import com.example.project.enums.Permission;
import com.example.project.services.AnnotationService;
import com.example.project.annotation.RequiresPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annotations")
@CrossOrigin(origins = "*")
public class AnnotationController {

    @Autowired
    private AnnotationService annotationService;

    

    @GetMapping("/version/{versionId}")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<List<AnnotationDTO>> getAnnotationsByVersion(@PathVariable Integer versionId) {
        try {
            List<AnnotationDTO> annotations = annotationService.getAnnotationsByVersion(versionId);
            return ResponseEntity.ok(annotations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    @PostMapping("/version/{versionId}")
    @RequiresPermission(Permission.EDIT_VERSION) 
    public ResponseEntity<AnnotationDTO> createAnnotation(
            @PathVariable Integer versionId,
            @RequestBody AnnotationDTO annotationDTO,
            Authentication authentication) {
        try {
            Integer userId = (Integer) authentication.getPrincipal();
            AnnotationDTO createdAnnotation = annotationService.createAnnotation(versionId, userId, annotationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAnnotation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    

    @PutMapping("/{annotationId}")
    @RequiresPermission(Permission.EDIT_VERSION)
    public ResponseEntity<AnnotationDTO> updateAnnotation(
            @PathVariable Integer annotationId,
            @RequestBody AnnotationDTO annotationDTO,
            Authentication authentication) {
        try {
            Integer userId = (Integer) authentication.getPrincipal();
            AnnotationDTO updatedAnnotation = annotationService.updateAnnotation(annotationId, userId, annotationDTO);
            return ResponseEntity.ok(updatedAnnotation);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    

    @DeleteMapping("/{annotationId}")
    @RequiresPermission(Permission.EDIT_VERSION)
    public ResponseEntity<Void> deleteAnnotation(
            @PathVariable Integer annotationId,
            Authentication authentication) {
        try {
            Integer userId = (Integer) authentication.getPrincipal();
            annotationService.deleteAnnotation(annotationId, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    

    @GetMapping("/{annotationId}")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<AnnotationDTO> getAnnotationById(@PathVariable Integer annotationId) {
        try {
            AnnotationDTO annotation = annotationService.getAnnotationById(annotationId);
            return ResponseEntity.ok(annotation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    

    @GetMapping("/version/{versionId}/type/{type}")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<List<AnnotationDTO>> getAnnotationsByType(
            @PathVariable Integer versionId,
            @PathVariable AnnotationType type) {
        try {
            List<AnnotationDTO> annotations = annotationService.getAnnotationsByVersionAndType(versionId, type);
            return ResponseEntity.ok(annotations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    

    @GetMapping("/version/{versionId}/count")
    @RequiresPermission(Permission.VIEW_VERSION)
    public ResponseEntity<Long> getAnnotationsCount(@PathVariable Integer versionId) {
        try {
            Long count = annotationService.countAnnotationsByVersion(versionId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
