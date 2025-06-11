package com.example.project.DTO;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private UserSummaryDTO recipient;
    private String message;
    private boolean isRead;
    private ProjectSummaryDTO project;
    private String entityType;
    private Integer entityId;
    private UserSummaryDTO triggeredBy;
    private LocalDateTime createdAt;

    public NotificationDTO() {
    }

    public NotificationDTO(Long id, UserSummaryDTO recipient, String message, boolean isRead, 
                          ProjectSummaryDTO project, String entityType, Integer entityId, 
                          UserSummaryDTO triggeredBy, LocalDateTime createdAt) {
        this.id = id;
        this.recipient = recipient;
        this.message = message;
        this.isRead = isRead;
        this.project = project;
        this.entityType = entityType;
        this.entityId = entityId;
        this.triggeredBy = triggeredBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserSummaryDTO getRecipient() {
        return recipient;
    }

    public void setRecipient(UserSummaryDTO recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public ProjectSummaryDTO getProject() {
        return project;
    }

    public void setProject(ProjectSummaryDTO project) {
        this.project = project;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public UserSummaryDTO getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(UserSummaryDTO triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
