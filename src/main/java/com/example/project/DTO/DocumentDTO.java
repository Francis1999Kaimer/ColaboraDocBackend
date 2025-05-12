package com.example.project.DTO;

import java.time.LocalDateTime;

public class DocumentDTO {
    private Integer iddocument;
    private String name;
    private String description;
    private Integer folderId;
    private UserSummaryDTO createdBy;
    private LocalDateTime createdAt;

    public DocumentDTO() {
    }

    public DocumentDTO(Integer iddocument, String name, String description, Integer folderId, UserSummaryDTO createdBy, LocalDateTime createdAt) {
        this.iddocument = iddocument;
        this.name = name;
        this.description = description;
        this.folderId = folderId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Integer getIddocument() {
        return iddocument;
    }

    public void setIddocument(Integer iddocument) {
        this.iddocument = iddocument;
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

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    public UserSummaryDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserSummaryDTO createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}