package com.example.project.controllers;

import com.example.project.DTO.VersionCreateRequestDTO;
import com.example.project.DTO.VersionDTO;
import com.example.project.services.VersionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VersionController {

    @Autowired
    private VersionService versionService;

    @PostMapping("/versions")
    public ResponseEntity<VersionDTO> createVersion(@Valid @RequestBody VersionCreateRequestDTO requestDTO) {
        VersionDTO createdVersion = versionService.createVersion(requestDTO);
        return new ResponseEntity<>(createdVersion, HttpStatus.CREATED);
    }

    @GetMapping("/documents/{documentId}/versions")
    public ResponseEntity<List<VersionDTO>> getVersionsByDocumentId(@PathVariable Integer documentId) {
        List<VersionDTO> versions = versionService.getVersionsForDocument(documentId);
        return ResponseEntity.ok(versions);
    }
}