package com.example.project.services;

import com.example.project.DTO.FolderCreateRequestDTO;
import com.example.project.DTO.FolderDTO;
import com.example.project.entities.Folder;
import com.example.project.entities.Project;
import com.example.project.repositories.FolderRepository;
import com.example.project.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private dtoMapper dtoMapper;    @Autowired
    private SoftDeleteService softDeleteService;    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DocumentService documentService;

    @Transactional
    public FolderDTO createFolder(FolderCreateRequestDTO requestDTO) {
        if (requestDTO.getProjectId() == null && requestDTO.getParentFolderId() == null) {
            throw new IllegalArgumentException("Se debe proporcionar projectId o parentFolderId.");
        }
        if (requestDTO.getProjectId() != null && requestDTO.getParentFolderId() != null) {
            throw new IllegalArgumentException("No se puede proporcionar projectId y parentFolderId simultáneamente.");
        }

        Folder folder = new Folder();
        folder.setName(requestDTO.getName());
        folder.setDescription(requestDTO.getDescription());


        if (requestDTO.getProjectId() != null) {
            Project project = projectRepository.findByIdAndActive(requestDTO.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + requestDTO.getProjectId()));
            folder.setProject(project);
        }

        if (requestDTO.getParentFolderId() != null) {
            Folder parentFolder = folderRepository.findByIdAndActive(requestDTO.getParentFolderId())
                    .orElseThrow(() -> new EntityNotFoundException("Carpeta padre no encontrada con ID: " + requestDTO.getParentFolderId()));
            folder.setParentFolder(parentFolder);
        
            if (folder.getProject() == null && parentFolder.getProject() != null) {
                folder.setProject(parentFolder.getProject());
            }
        }

        Folder savedFolder = folderRepository.save(folder);
        return dtoMapper.toFolderDTO(savedFolder);
    }

    @Transactional(readOnly = true)
    public FolderDTO getFolderById(Integer folderId) {
        Folder folder = folderRepository.findByIdAndActive(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Carpeta no encontrada con ID: " + folderId));
        return dtoMapper.toFolderDTO(folder);
    }

    @Transactional(readOnly = true)
    public List<FolderDTO> getActiveFoldersByParent(Integer parentFolderId) {
        Folder parentFolder = folderRepository.findByIdAndActive(parentFolderId)
                .orElseThrow(() -> new EntityNotFoundException("Carpeta padre no encontrada con ID: " + parentFolderId));
        
        return folderRepository.findActiveByParentFolder(parentFolder)
                .stream()
                .map(dtoMapper::toFolderDTO)
                .collect(Collectors.toList());
    }    
    
    @Transactional
    public void deleteFolder(Integer folderId, String deletedBy) {
        Folder folder = folderRepository.findByIdAndActive(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Carpeta no encontrada con ID: " + folderId));
        
        deleteFolderCascade(folder, deletedBy);
    }

    

    private void deleteFolderCascade(Folder folder, String deletedBy) {
        
        folder.getDocuments().forEach(document -> {
            if (!document.isDeleted()) {
                try {
                    documentService.deleteDocument(document.getIddocument(), deletedBy);
                } catch (Exception e) {
                    
                }
            }
        });

        
        folder.getChildFolders().forEach(childFolder -> {
            if (!childFolder.isDeleted()) {
                deleteFolderCascade(childFolder, deletedBy);
            }
        });

        
        softDeleteService.softDelete(folder, deletedBy);
        folderRepository.save(folder);
        
        
        try {
            notificationService.cleanupNotificationsForEntity("FOLDER", folder.getIdfolder(), deletedBy);
        } catch (Exception e) {
            
        }
    }    
      @Transactional
    public void restoreFolder(Integer folderId) {
        restoreFolder(folderId, null);
    }
    
    @Transactional
    public void restoreFolder(Integer folderId, Integer newParentFolderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Carpeta no encontrada con ID: " + folderId));
        
        if (!folder.isDeleted()) {
            throw new IllegalStateException("La carpeta con ID " + folderId + " no está eliminada");
        }
        
        
        if (newParentFolderId != null) {
            Folder newParentFolder = folderRepository.findByIdAndActive(newParentFolderId)
                    .orElseThrow(() -> new EntityNotFoundException("Carpeta padre no encontrada o no activa con ID: " + newParentFolderId));
            
            
            if (!newParentFolder.getProject().getIdproject().equals(folder.getProject().getIdproject())) {
                throw new IllegalStateException("La carpeta padre especificada no pertenece al mismo proyecto");
            }
            
            
            if (isDescendantOf(newParentFolder, folder)) {
                throw new IllegalStateException("No se puede mover la carpeta a uno de sus descendientes");
            }
            
            folder.setParentFolder(newParentFolder);
        } else {
            
            if (folder.getParentFolder() != null && folder.getParentFolder().isDeleted()) {
                throw new IllegalStateException("No se puede restaurar la carpeta porque su carpeta padre está eliminada. " +
                        "Primero debe restaurar la carpeta padre con ID: " + folder.getParentFolder().getIdfolder() +
                        " o especifique un nuevo padre activo");
            }
        }
        
        
        
        folder.restore();
        folderRepository.save(folder);
    }
    
    

    private boolean isDescendantOf(Folder targetFolder, Folder potentialAncestor) {
        Folder current = targetFolder.getParentFolder();
        while (current != null) {
            if (current.getIdfolder().equals(potentialAncestor.getIdfolder())) {
                return true;
            }
            current = current.getParentFolder();
        }
        return false;
    }    

    @Transactional
    public void restoreFolderCascade(Integer folderId) {
        restoreFolderCascade(folderId, null);
    }
    
    @Transactional
    public void restoreFolderCascade(Integer folderId, Integer newParentFolderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Carpeta no encontrada con ID: " + folderId));
        
        if (!folder.isDeleted()) {
            throw new IllegalStateException("La carpeta con ID " + folderId + " no está eliminada");
        }
        
        
        if (newParentFolderId != null) {
            Folder newParentFolder = folderRepository.findByIdAndActive(newParentFolderId)
                    .orElseThrow(() -> new EntityNotFoundException("Carpeta padre no encontrada o no activa con ID: " + newParentFolderId));
            
            
            if (!newParentFolder.getProject().getIdproject().equals(folder.getProject().getIdproject())) {
                throw new IllegalStateException("La carpeta padre especificada no pertenece al mismo proyecto");
            }
            
            
            if (isDescendantOf(newParentFolder, folder)) {
                throw new IllegalStateException("No se puede mover la carpeta a uno de sus descendientes");
            }
            
            folder.setParentFolder(newParentFolder);
        } else {
            
            if (folder.getParentFolder() != null && folder.getParentFolder().isDeleted()) {
                throw new IllegalStateException("No se puede restaurar la carpeta porque su carpeta padre está eliminada. " +
                        "Primero debe restaurar la carpeta padre con ID: " + folder.getParentFolder().getIdfolder() +
                        " o especifique un nuevo padre activo");
            }
        }
        
        restoreFolderRecursive(folder);
    }

    private void restoreFolderRecursive(Folder folder) {
        
        if (folder.getParentFolder() != null && folder.getParentFolder().isDeleted()) {
            throw new IllegalStateException("No se puede restaurar la carpeta '" + folder.getName() + 
                    "' porque su carpeta padre está eliminada. ID carpeta padre: " + folder.getParentFolder().getIdfolder());
        }
        
        
        folder.restore();
        folderRepository.save(folder);
        
        
        folder.getDocuments().forEach(document -> {
            if (document.isDeleted()) {
                try {
                    documentService.restoreDocument(document.getIddocument());
                } catch (Exception e) {
                    
                }
            }
        });

        
        folder.getChildFolders().forEach(childFolder -> {
            if (childFolder.isDeleted()) {
                restoreFolderRecursive(childFolder);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<FolderDTO> getDeletedFoldersByParent(Integer parentFolderId) {
        Folder parentFolder = folderRepository.findById(parentFolderId)
                .orElseThrow(() -> new EntityNotFoundException("Carpeta padre no encontrada con ID: " + parentFolderId));
        
        return folderRepository.findDeletedByParentFolder(parentFolder)
                .stream()
                .map(dtoMapper::toFolderDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FolderDTO> getDeletedFoldersByProject(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));
        
        return folderRepository.findDeletedByProject(project)
                .stream()
                .map(dtoMapper::toFolderDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional(readOnly = true)
    public List<FolderDTO> getDeletedFoldersByProjectWithHierarchy(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));
        
        return folderRepository.findDeletedByProjectWithHierarchy(project)
                .stream()
                .map(dtoMapper::toDeletedFolderDTOWithHierarchy)
                .collect(Collectors.toList());
    }
}