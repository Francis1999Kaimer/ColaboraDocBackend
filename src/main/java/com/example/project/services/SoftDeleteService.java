package com.example.project.services;

import com.example.project.entities.base.AuditableEntity;
import com.example.project.enums.EntityStatus;
import org.springframework.stereotype.Service;



@Service
public class SoftDeleteService {

    

    public <T extends AuditableEntity> void softDelete(T entity) {
        entity.softDelete();
    }

    

    public <T extends AuditableEntity> void softDelete(T entity, String deletedBy) {
        entity.softDelete(deletedBy);
    }

    

    public <T extends AuditableEntity> void restore(T entity) {
        entity.restore();
    }

    

    public <T extends AuditableEntity> void archive(T entity) {
        entity.archive();
    }

    

    public <T extends AuditableEntity> boolean canDelete(T entity) {
        return entity.isActive();
    }

    

    public <T extends AuditableEntity> boolean canRestore(T entity) {
        return entity.isDeleted();
    }

    

    public boolean isEntityActive(AuditableEntity entity) {
        return entity != null && entity.isActive();
    }

    

    public boolean filterActive(AuditableEntity entity) {
        return entity.getStatus() == EntityStatus.ACTIVE;
    }

    

    public boolean filterDeleted(AuditableEntity entity) {
        return entity.getStatus() == EntityStatus.DELETED;
    }

    

    public boolean filterArchived(AuditableEntity entity) {
        return entity.getStatus() == EntityStatus.ARCHIVED;
    }
}
