package com.example.project.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VersionCreateRequestDTO {
    @NotNull(message = "El ID del documento es obligatorio.")
    private Integer documentId;

    @NotNull(message = "El número de versión es obligatorio.")
    private Integer versionNumber;

    @NotBlank(message = "El ID del archivo de Dropbox es obligatorio.")
    private String dropboxFileId;

    private String dropboxFilePath;
    private Long fileSize;
    private String mimeType;
    private String comments;

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
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
}