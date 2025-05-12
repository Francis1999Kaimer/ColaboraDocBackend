package com.example.project.DTO;

import java.time.LocalDateTime;

public class VersionDTO {
    private Integer idversion;
    private Integer versionNumber;
    private String dropboxFileId;
    private String dropboxFilePath;
    private Long fileSize;
    private String mimeType;
    private String comments;
    private Integer documentId;
    private UserSummaryDTO uploadedBy;
    private LocalDateTime uploadedAt;

    public VersionDTO() {
    }

    public VersionDTO(Integer idversion, Integer versionNumber, String dropboxFileId, String dropboxFilePath, Long fileSize, String mimeType, String comments, Integer documentId, UserSummaryDTO uploadedBy, LocalDateTime uploadedAt) {
        this.idversion = idversion;
        this.versionNumber = versionNumber;
        this.dropboxFileId = dropboxFileId;
        this.dropboxFilePath = dropboxFilePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.comments = comments;
        this.documentId = documentId;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public Integer getIdversion() {
        return idversion;
    }

    public void setIdversion(Integer idversion) {
        this.idversion = idversion;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getDropboxFileId() {
        return dropboxFileId;
    }

    public void setDropboxFileId(String dropboxFileId) {
        this.dropboxFileId = dropboxFileId;
    }

    public String getDropboxFilePath() {
        return dropboxFilePath;
    }

    public void setDropboxFilePath(String dropboxFilePath) {
        this.dropboxFilePath = dropboxFilePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public UserSummaryDTO getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UserSummaryDTO uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}