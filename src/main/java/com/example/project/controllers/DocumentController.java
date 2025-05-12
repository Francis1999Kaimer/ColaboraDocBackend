package com.example.project.controllers;

import com.example.project.DTO.DocumentCreateRequestDTO;
import com.example.project.DTO.DocumentDTO;
import com.example.project.services.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentDTO> createDocument(@Valid @RequestBody DocumentCreateRequestDTO requestDTO) {
        DocumentDTO createdDocument = documentService.createDocument(requestDTO);
        return new ResponseEntity<>(createdDocument, HttpStatus.CREATED);
    }
}