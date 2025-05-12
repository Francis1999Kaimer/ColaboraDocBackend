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

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private dtoMapper dtoMapper;

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
            Project project = projectRepository.findById(requestDTO.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + requestDTO.getProjectId()));
            folder.setProject(project);
        }

        if (requestDTO.getParentFolderId() != null) {
            Folder parentFolder = folderRepository.findById(requestDTO.getParentFolderId())
                    .orElseThrow(() -> new EntityNotFoundException("Carpeta padre no encontrada con ID: " + requestDTO.getParentFolderId()));
            folder.setParentFolder(parentFolder);
        
            if (folder.getProject() == null && parentFolder.getProject() != null) {
                folder.setProject(parentFolder.getProject());
            }
        }


        Folder savedFolder = folderRepository.save(folder);
        return dtoMapper.toFolderDTO(savedFolder);
    }
}