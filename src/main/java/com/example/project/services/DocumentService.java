package com.example.project.services;

import com.example.project.DTO.DocumentCreateRequestDTO;
import com.example.project.DTO.DocumentDTO;
import com.example.project.DTO.DocumentWithFolderDTO;
import com.example.project.entities.Document;
import com.example.project.entities.Folder;
import com.example.project.entities.Project;
import com.example.project.entities.Version;
import com.example.project.repositories.DocumentRepository;
import com.example.project.repositories.FolderRepository;
import com.example.project.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private dtoMapper dtoMapper;

    @Autowired
    private SoftDeleteService softDeleteService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VersionService versionService;@Transactional
    public DocumentDTO createDocument(DocumentCreateRequestDTO requestDTO) {
        Folder folder = folderRepository.findByIdAndActive(requestDTO.getFolderId())
                .orElseThrow(() -> new EntityNotFoundException("Carpeta no encontrada con ID: " + requestDTO.getFolderId()));

        Document document = new Document();
        document.setName(requestDTO.getName());
        document.setDescription(requestDTO.getDescription());
        document.setFolder(folder);

        Document savedDocument = documentRepository.save(document);
        return dtoMapper.toDocumentDTO(savedDocument);
    }

    @Transactional(readOnly = true)
    public DocumentDTO getDocumentById(Integer documentId) {
        Document document = documentRepository.findByIdAndActive(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));
        return dtoMapper.toDocumentDTO(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByFolderId(Integer folderId) {
        return documentRepository.findActiveByFolderId(folderId)
                .stream()
                .map(dtoMapper::toDocumentDTO)
                .collect(Collectors.toList());
    }    
    
    @Transactional
    public void deleteDocument(Integer documentId, String deletedBy) {
        Document document = documentRepository.findByIdAndActive(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));
        
        
        deleteDocumentCascade(document, deletedBy);
    }

    

    private void deleteDocumentCascade(Document document, String deletedBy) {
        
        List<Version> activeVersions = document.getVersions().stream()
                .filter(version -> !version.isDeleted())
                .collect(Collectors.toList());
        
        for (Version version : activeVersions) {
            versionService.deleteVersion(version.getIdversion(), deletedBy);
        }
        
        
        softDeleteService.softDelete(document, deletedBy);
        documentRepository.save(document);
        
        
        try {
            notificationService.cleanupNotificationsForEntity("DOCUMENT", document.getIddocument(), deletedBy);
        } catch (Exception e) {
            
        }
    }    @Transactional
    public void restoreDocument(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));
        
        if (!document.isDeleted()) {
            throw new IllegalStateException("El documento con ID " + documentId + " no está eliminado");
        }
        
        
        if (document.getFolder() != null && document.getFolder().isDeleted()) {
            throw new IllegalStateException("No se puede restaurar el documento porque su carpeta padre está eliminada");
        }
        
        
        restoreDocumentCascade(document);
    }

    

    private void restoreDocumentCascade(Document document) {
        
        document.restore();
        documentRepository.save(document);
        
        
        List<Version> deletedVersions = document.getVersions().stream()
                .filter(Version::isDeleted)
                .collect(Collectors.toList());
        
        for (Version version : deletedVersions) {
            versionService.restoreVersion(version.getIdversion());
        }
    }

    

    @Transactional
    public void restoreDocumentCascade(Integer documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));
        
        if (!document.isDeleted()) {
            throw new IllegalStateException("El documento con ID " + documentId + " no está eliminado");
        }
        
        
        if (document.getFolder() != null && document.getFolder().isDeleted()) {
            throw new IllegalStateException("No se puede restaurar el documento porque su carpeta padre está eliminada");
        }
        
        restoreDocumentCascade(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getDeletedDocumentsByFolderId(Integer folderId) {
        return documentRepository.findDeletedByFolderId(folderId)
                .stream()
                .map(dtoMapper::toDocumentDTO)
                .collect(Collectors.toList());
    }      

    @Transactional(readOnly = true)
    public List<DocumentWithFolderDTO> getDeletedDocumentsByProject(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));
        
        
        return documentRepository.findDeletedByProjectWithParent(project)
                .stream()
                .map(dtoMapper::toDocumentWithFolderDTO)
                .collect(Collectors.toList());
    }
}