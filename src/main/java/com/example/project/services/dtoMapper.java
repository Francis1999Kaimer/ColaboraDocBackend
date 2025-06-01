package com.example.project.services;

import com.example.project.DTO.*;
import com.example.project.entities.*;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.Collections;

@Component
public class dtoMapper {

    public UserSummaryDTO toUserSummaryDTO(User user) {
        if (user == null) return null;
        return new UserSummaryDTO(user.getIduser(), user.getEmail(), user.getNames(), user.getLastnames());
    }

    public FolderDTO toFolderDTO(Folder folder) {
        if (folder == null) return null;
        return new FolderDTO(
                folder.getIdfolder(),
                folder.getName(),
                folder.getDescription(),
                folder.getProject() != null ? folder.getProject().getIdproject() : null,
                folder.getParentFolder() != null ? folder.getParentFolder().getIdfolder() : null,
                toUserSummaryDTO(folder.getCreatedBy()),
                folder.getCreatedAt()
        );
    }

    public FolderDTO toFolderDTOWithHierarchy(Folder folder) {
        if (folder == null) return null;

        FolderDTO dto = new FolderDTO();
        dto.setIdfolder(folder.getIdfolder());
        dto.setName(folder.getName());
        dto.setDescription(folder.getDescription());
        if (folder.getProject() != null) {
            dto.setProjectId(folder.getProject().getIdproject());
        }
        if (folder.getParentFolder() != null) {
            dto.setParentFolderId(folder.getParentFolder().getIdfolder());
        }
 
        dto.setCreatedBy(toUserSummaryDTO(folder.getCreatedBy()));
        dto.setCreatedAt(folder.getCreatedAt());


        if (folder.getDocuments() != null) {
            dto.setDocuments(folder.getDocuments().stream()
                    .map(this::toDocumentDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setDocuments(Collections.emptyList()); 
        }

  
        if (folder.getChildFolders() != null) {
            dto.setChildFolders(folder.getChildFolders().stream()
                    .map(this::toFolderDTOWithHierarchy)
                    .collect(Collectors.toList()));
        } else {
            dto.setChildFolders(Collections.emptyList());
        }
        return dto;
    }
    

    public DocumentDTO toDocumentDTO(Document document) {
        if (document == null) return null;
        DocumentDTO dto = new DocumentDTO();
        dto.setIddocument(document.getIddocument());
        dto.setName(document.getName());
        dto.setDescription(document.getDescription());
        if (document.getFolder() != null) {
            dto.setFolderId(document.getFolder().getIdfolder());
        }
        dto.setCreatedBy(toUserSummaryDTO(document.getCreatedBy()));
        dto.setCreatedAt(document.getCreatedAt());

        return dto;
    }

    public VersionDTO toVersionDTO(Version version) {
        if (version == null) return null;
        return new VersionDTO(
                version.getIdversion(),
                version.getVersionNumber(),
                version.getDropboxFileId(),
                version.getDropboxFilePath(),
                version.getFileSize(),
                version.getMimeType(),
                version.getComments(),
                version.getDocument() != null ? version.getDocument().getIddocument() : null,
                toUserSummaryDTO(version.getUploadedBy()),
                version.getUploadedAt()
        );
    }

    

    public ProjectSummaryDTO toProjectSummaryDTO(Project project) {
        if (project == null) return null;
        return new ProjectSummaryDTO(project.getIdproject(), project.getName());
    }

    public ProjectUserDTO toProjectUserDTO(ProjectUser projectUser) {
        if (projectUser == null) return null;
        return new ProjectUserDTO(
            projectUser.getId(),
            toUserSummaryDTO(projectUser.getUser()),
            projectUser.getRoleCode(),
            projectUser.getStatus() != null ? projectUser.getStatus().name() : null,
            toProjectSummaryDTO(projectUser.getProject()),
            toUserSummaryDTO(projectUser.getInvitedBy()), 
            projectUser.getInvitationDate() 
        );
    }

     
    public ProjectDTO toProjectDTO(Project project) {
        if (project == null) return null;
        ProjectDTO dto = new ProjectDTO(project.getIdproject(), project.getName(), project.getDescription());

        return dto;
    }
}