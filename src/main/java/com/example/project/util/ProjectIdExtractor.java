package com.example.project.util;

import com.example.project.entities.*;
import com.example.project.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;



@Component
public class ProjectIdExtractor {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private VersionRepository versionRepository;

    

    public Integer getProjectIdFromFolder(Integer folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Carpeta no encontrada con ID: " + folderId));
        return folder.getProject().getIdproject();
    }

    

    public Integer getProjectIdFromDocument(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));
        return document.getFolder().getProject().getIdproject();
    }

    

    public Integer getProjectIdFromVersion(Integer versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada con ID: " + versionId));
        return version.getDocument().getFolder().getProject().getIdproject();
    }
}
