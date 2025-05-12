package com.example.project.entities;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "versions")
@EntityListeners(AuditingEntityListener.class)
public class Version {
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
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;


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
    }

    public Version(Integer versionNumber, String dropboxFileId, Document document) {
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
    public void setDocument(Document document) { this.document = document; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }


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