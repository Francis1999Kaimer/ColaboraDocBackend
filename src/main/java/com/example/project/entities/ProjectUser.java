package com.example.project.entities;

import com.example.project.entities.base.AuditableEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_user")
@EntityListeners(AuditingEntityListener.class) 
public class ProjectUser extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iduser", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproject", nullable = false)
    private Project project;    @Column(nullable = false)
    private String roleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_invitacion", nullable = false, length = 20)
    private InvitationStatus statusInvitacion = InvitationStatus.PENDING;

    @CreatedBy 
    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "invited_by_user_id", nullable = false, updatable = false)
    private User invitedBy;

    @CreatedDate
    @Column(name = "invitation_date", nullable = false, updatable = false)
    private LocalDateTime invitationDate;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public ProjectUser() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public InvitationStatus getStatusInvitacion() { return statusInvitacion; }
    public void setStatusInvitacion(InvitationStatus statusInvitacion) { this.statusInvitacion = statusInvitacion; }
    public User getInvitedBy() { return invitedBy; }
    public void setInvitedBy(User invitedBy) { this.invitedBy = invitedBy; }
    public LocalDateTime getInvitationDate() { return invitationDate; }
    public void setInvitationDate(LocalDateTime invitationDate) { this.invitationDate = invitationDate; }
    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
}