package com.example.project.entities.base;

import com.example.project.enums.EntityStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;



@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    
    public AuditableEntity() {
        this.status = EntityStatus.ACTIVE;
    }

    
    

    public void softDelete() {
        this.status = EntityStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    

    public void softDelete(String deletedBy) {
        this.status = EntityStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    

    public void restore() {
        this.status = EntityStatus.ACTIVE;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    

    public void archive() {
        this.status = EntityStatus.ARCHIVED;
    }

    

    public boolean isActive() {
        return status == EntityStatus.ACTIVE;
    }

    

    public boolean isDeleted() {
        return status == EntityStatus.DELETED;
    }

    

    public boolean isArchived() {
        return status == EntityStatus.ARCHIVED;
    }

    
    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
