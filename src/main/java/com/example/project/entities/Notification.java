package com.example.project.entities;

import com.example.project.entities.base.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient; 

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproject", nullable = true) 
    private Project project;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id") 
    private Integer entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_user_id") 
    private User triggeredBy;

    public Notification() {
        super(); 
    }    public Notification(User recipient, String message, Project project, String entityType, Integer entityId, User triggeredBy) {
        super(); 
        this.recipient = recipient;
        this.message = message;
        this.project = project;
        this.entityType = entityType;
        this.entityId = entityId;
        this.triggeredBy = triggeredBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }
    public User getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(User triggeredBy) { this.triggeredBy = triggeredBy; }
}

