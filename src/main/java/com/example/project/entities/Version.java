package com.example.project.entities;

import com.example.project.entities.base.AuditableEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "versions")
public class Version extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idversion;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(name = "dropbox_file_id", nullable = false, unique = true)
    private String dropboxFileId;

    @Column(name = "dropbox_file_path")
    private String dropboxFilePath;

    @Column(name = "file_size_bytes")
    private Long fileSize;    @Column(name = "mime_type")
    private String mimeType;

    
    @Column(name = "processed_dropbox_file_id")
    private String processedDropboxFileId;

    @Column(name = "processed_dropbox_file_path")
    private String processedDropboxFilePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Column(name = "processing_error_message")
    private String processingErrorMessage;

    @CreatedDate 
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column
    private String comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iddocument", nullable = false)
    private Document document;


    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false, updatable = false)
    private User uploadedBy;
    public Version() {
        super(); 
    }

    public Version(Integer versionNumber, String dropboxFileId, Document document) {
        super(); 
        this.versionNumber = versionNumber;
        this.dropboxFileId = dropboxFileId;
        this.document = document;
    }

    public Integer getIdversion() { return idversion; }
    public void setIdversion(Integer idversion) { this.idversion = idversion; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getDropboxFileId() { return dropboxFileId; }
    public void setDropboxFileId(String dropboxFileId) { this.dropboxFileId = dropboxFileId; }
    public String getDropboxFilePath() { return dropboxFilePath; }
    public void setDropboxFilePath(String dropboxFilePath) { this.dropboxFilePath = dropboxFilePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    
    public String getProcessedDropboxFileId() { return processedDropboxFileId; }
    public void setProcessedDropboxFileId(String processedDropboxFileId) { this.processedDropboxFileId = processedDropboxFileId; }
    public String getProcessedDropboxFilePath() { return processedDropboxFilePath; }
    public void setProcessedDropboxFilePath(String processedDropboxFilePath) { this.processedDropboxFilePath = processedDropboxFilePath; }
    public ProcessingStatus getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(ProcessingStatus processingStatus) { this.processingStatus = processingStatus; }
    public String getProcessingErrorMessage() { return processingErrorMessage; }
    public void setProcessingErrorMessage(String processingErrorMessage) { this.processingErrorMessage = processingErrorMessage; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version version = (Version) o;
        return idversion != null && idversion.equals(version.idversion);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}