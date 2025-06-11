package com.example.project.services;

import com.example.project.DTO.VersionCreateRequestDTO;
import com.example.project.DTO.VersionDTO;
import com.example.project.entities.Document;
import com.example.project.entities.Project;
import com.example.project.entities.ProjectUser;
import com.example.project.entities.User;
import com.example.project.entities.Version;
import com.example.project.repositories.DocumentRepository;
import com.example.project.repositories.ProjectRepository;
import com.example.project.repositories.ProjectUserRepository;
import com.example.project.repositories.VersionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionService {

    private static final Logger logger = LoggerFactory.getLogger(VersionService.class);

    @Autowired
    private VersionRepository versionRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private dtoMapper dtoMapper;

    @Autowired
    private DropboxService dropboxService;

    @Autowired
    private SoftDeleteService softDeleteService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public VersionDTO createVersion(VersionCreateRequestDTO requestDTO, User currentUser) {
        Document document = documentRepository.findByIdAndActive(requestDTO.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + requestDTO.getDocumentId()));

        Version version = new Version();
        version.setDocument(document);
        version.setVersionNumber(requestDTO.getVersionNumber());
        version.setDropboxFileId(requestDTO.getDropboxFileId());
        version.setDropboxFilePath(requestDTO.getDropboxFilePath());
        version.setFileSize(requestDTO.getFileSize());
        version.setMimeType(requestDTO.getMimeType());
        version.setComments(requestDTO.getComments());
        version.setUploadedBy(currentUser);

        Version savedVersion = versionRepository.save(version);

        
        createVersionNotifications(document, savedVersion, currentUser);

        return dtoMapper.toVersionDTO(savedVersion);
    }

    
    @Transactional
    public VersionDTO createVersion(VersionCreateRequestDTO requestDTO) {
        Document document = documentRepository.findByIdAndActive(requestDTO.getDocumentId())
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + requestDTO.getDocumentId()));

        Version version = new Version();
        version.setDocument(document);
        version.setVersionNumber(requestDTO.getVersionNumber());
        version.setDropboxFileId(requestDTO.getDropboxFileId());
        version.setDropboxFilePath(requestDTO.getDropboxFilePath());
        version.setFileSize(requestDTO.getFileSize());
        version.setMimeType(requestDTO.getMimeType());
        version.setComments(requestDTO.getComments());

        Version savedVersion = versionRepository.save(version);
        return dtoMapper.toVersionDTO(savedVersion);
    }    

    @Transactional(readOnly = true)
    public VersionDTO getVersionById(Integer versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Version not found with id: " + versionId));
        return dtoMapper.toVersionDTO(version);
    }

    @Transactional(readOnly = true)
    public List<VersionDTO> getVersionsForDocument(Integer documentId) {
        
        documentRepository.findByIdAndActive(documentId)
            .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));

        
        return versionRepository.findActiveByDocumentId(documentId).stream() 
                .map(dtoMapper::toVersionDTO)
                .collect(Collectors.toList());
    }    @Transactional
    public VersionDTO createVersionWithFile(Integer documentId, Integer versionNumber, String comments, MultipartFile file, User currentUser) {
        try {
            
            Document document = documentRepository.findByIdAndActive(documentId)
                    .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));

            
            Integer projectId = document.getFolder().getProject().getIdproject();
            Integer folderId = document.getFolder().getIdfolder();
            
            
            String folderPath = dropboxService.generateDocumentFolderPath(projectId, folderId, documentId, document.getName());
            
            
            DropboxService.DropboxUploadResult uploadResult = dropboxService.uploadFile(file, folderPath);

            
            Version version = new Version();
            version.setDocument(document);
            version.setVersionNumber(versionNumber);
            version.setDropboxFileId(uploadResult.getFileId());
            version.setDropboxFilePath(uploadResult.getFilePath());
            version.setFileSize(uploadResult.getFileSize());
            version.setMimeType(uploadResult.getMimeType());
            version.setComments(comments);
            version.setUploadedBy(currentUser);

            Version savedVersion = versionRepository.save(version);

            
            createVersionNotifications(document, savedVersion, currentUser);

            return dtoMapper.toVersionDTO(savedVersion);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al crear la versión: " + e.getMessage(), e);
        }
    }    
    @Transactional
    public VersionDTO createVersionWithFile(Integer documentId, Integer versionNumber, String comments, MultipartFile file) {
        try {
            
            Document document = documentRepository.findByIdAndActive(documentId)
                    .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));

            
            Integer projectId = document.getFolder().getProject().getIdproject();
            Integer folderId = document.getFolder().getIdfolder();
            
            
            String folderPath = dropboxService.generateDocumentFolderPath(projectId, folderId, documentId, document.getName());
            
            
            DropboxService.DropboxUploadResult uploadResult = dropboxService.uploadFile(file, folderPath);

            
            Version version = new Version();
            version.setDocument(document);
            version.setVersionNumber(versionNumber);
            version.setDropboxFileId(uploadResult.getFileId());
            version.setDropboxFilePath(uploadResult.getFilePath());
            version.setFileSize(uploadResult.getFileSize());
            version.setMimeType(uploadResult.getMimeType());
            version.setComments(comments);

            Version savedVersion = versionRepository.save(version);
            return dtoMapper.toVersionDTO(savedVersion);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al crear la versión: " + e.getMessage(), e);
        }
    }

    private void createVersionNotifications(Document document, Version version, User currentUser) {
        try {
            
            Project project = null;
            if (document.getFolder() != null) {
                project = document.getFolder().getProject();
            }

            if (project != null) {
                
                List<ProjectUser> projectMembers = projectUserRepository.findByProject(project)
                    .stream()
                    .filter(pu -> pu.getStatusInvitacion() == ProjectUser.InvitationStatus.ACCEPTED && 
                                  !pu.getUser().getIduser().equals(currentUser.getIduser()))
                    .collect(Collectors.toList());
                
                String notificationMessage = String.format(
                    "Nueva versión %d creada en el documento '%s' por %s %s", 
                    version.getVersionNumber(),
                    document.getName(), 
                    currentUser.getNames(), 
                    currentUser.getLastnames()
                );

                for (ProjectUser member : projectMembers) {
                    notificationService.createNotification(
                        member.getUser(), 
                        notificationMessage, 
                        project, 
                        "VERSION_CREATED", 
                        version.getIdversion(), 
                        currentUser
                    );
                }

                logger.info("Creadas {} notificaciones para nueva versión {} del documento '{}'", 
                           projectMembers.size(), version.getVersionNumber(), document.getName());
            }
        } catch (Exception e) {
            logger.error("Error al crear notificaciones para versión del documento ID {}: {}", 
                        document.getIddocument(), e.getMessage());
        }
    }

    

    @Transactional
    public void deleteVersion(Integer versionId, String deletedBy) {
        Version version = versionRepository.findByIdAndActive(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada con ID: " + versionId));
        
        
        softDeleteService.softDelete(version, deletedBy);
        versionRepository.save(version);
    }    

    @Transactional
    public void restoreVersion(Integer versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Versión no encontrada con ID: " + versionId));
        
        if (!version.isDeleted()) {
            throw new IllegalStateException("La versión no está eliminada");
        }
        
        
        if (version.getDocument() != null && version.getDocument().isDeleted()) {
            throw new IllegalStateException("No se puede restaurar la versión porque su documento padre está eliminado");
        }
        
        softDeleteService.restore(version);
        versionRepository.save(version);
    }    

    @Transactional(readOnly = true)
    public List<VersionDTO> getDeletedVersionsForDocument(Integer documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con ID: " + documentId));

        return document.getVersions().stream()
                .filter(Version::isDeleted)
                .map(dtoMapper::toVersionDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional(readOnly = true)
    public List<VersionDTO> getDeletedVersionsForProject(Integer projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));

        return versionRepository.findDeletedByProject(project).stream()
                .map(dtoMapper::toVersionDTO)
                .collect(Collectors.toList());
    }
}