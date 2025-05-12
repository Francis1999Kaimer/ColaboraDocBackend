package com.example.project.controllers;

import com.example.project.DTO.FolderCreateRequestDTO;
import com.example.project.DTO.FolderDTO;
import com.example.project.services.FolderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderDTO> createFolder(@Valid @RequestBody FolderCreateRequestDTO requestDTO) {
        FolderDTO createdFolder = folderService.createFolder(requestDTO);
        return new ResponseEntity<>(createdFolder, HttpStatus.CREATED);
    }
}