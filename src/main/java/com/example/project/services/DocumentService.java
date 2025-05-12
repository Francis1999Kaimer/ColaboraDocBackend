package com.example.project.services;

import com.example.project.DTO.DocumentCreateRequestDTO;
import com.example.project.DTO.DocumentDTO;
import com.example.project.entities.Document;
import com.example.project.entities.Folder;
import com.example.project.repositories.DocumentRepository;
import com.example.project.repositories.FolderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private dtoMapper dtoMapper;

    @Transactional
    public DocumentDTO createDocument(DocumentCreateRequestDTO requestDTO) {
        Folder folder = folderRepository.findById(requestDTO.getFolderId())
                .orElseThrow(() -> new EntityNotFoundException("Carpeta no encontrada con ID: " + requestDTO.getFolderId()));

        Document document = new Document();
        document.setName(requestDTO.getName());
        document.setDescription(requestDTO.getDescription());
        document.setFolder(folder);
       

        Document savedDocument = documentRepository.save(document);
        return dtoMapper.toDocumentDTO(savedDocument);
    }
}