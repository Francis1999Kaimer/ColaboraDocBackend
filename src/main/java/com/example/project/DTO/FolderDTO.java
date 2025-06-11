package com.example.project.DTO;

import com.example.project.enums.FolderType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class FolderDTO {
    private Integer idfolder;
    private String name;
    private String description;
    private FolderType folderType;
    private Integer projectId; 
    private Integer parentFolderId;
    private UserSummaryDTO createdBy;
    private LocalDateTime createdAt;

    
    private List<FolderDTO> childFolders = new ArrayList<>(); 
    private List<DocumentDTO> documents = new ArrayList<>();  

    public FolderDTO() {
    }    
    public FolderDTO(Integer idfolder, String name, String description, FolderType folderType, Integer projectId, 
                     Integer parentFolderId, UserSummaryDTO createdBy, LocalDateTime createdAt) {
        this.idfolder = idfolder;
        this.name = name;
        this.description = description;
        this.folderType = folderType;
        this.projectId = projectId;
        this.parentFolderId = parentFolderId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    
    public Integer getIdfolder() { return idfolder; }
    public void setIdfolder(Integer idfolder) { this.idfolder = idfolder; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public FolderType getFolderType() { return folderType; }
    public void setFolderType(FolderType folderType) { this.folderType = folderType; }
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public Integer getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(Integer parentFolderId) { this.parentFolderId = parentFolderId; }
    public UserSummaryDTO getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserSummaryDTO createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<FolderDTO> getChildFolders() { return childFolders; }
    public void setChildFolders(List<FolderDTO> childFolders) { this.childFolders = childFolders; }
    public List<DocumentDTO> getDocuments() { return documents; }
    public void setDocuments(List<DocumentDTO> documents) { this.documents = documents; }
}