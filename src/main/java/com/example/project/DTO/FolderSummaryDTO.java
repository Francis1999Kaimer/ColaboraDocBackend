package com.example.project.DTO;

import com.example.project.enums.FolderType;

public class FolderSummaryDTO {
    private Integer idfolder;
    private String name;
    private String description;
    private FolderType folderType;
    private Integer projectId;
    private Integer parentFolderId;

    public FolderSummaryDTO() {
    }

    public FolderSummaryDTO(Integer idfolder, String name, String description, FolderType folderType, 
                           Integer projectId, Integer parentFolderId) {
        this.idfolder = idfolder;
        this.name = name;
        this.description = description;
        this.folderType = folderType;
        this.projectId = projectId;
        this.parentFolderId = parentFolderId;
    }

    public Integer getIdfolder() {
        return idfolder;
    }

    public void setIdfolder(Integer idfolder) {
        this.idfolder = idfolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FolderType getFolderType() {
        return folderType;
    }

    public void setFolderType(FolderType folderType) {
        this.folderType = folderType;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Integer parentFolderId) {
        this.parentFolderId = parentFolderId;
    }
}
