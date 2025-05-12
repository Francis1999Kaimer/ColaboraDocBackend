package com.example.project.DTO;

import java.time.LocalDateTime;

public class ProjectUserDTO {
    private Integer id;
    private UserSummaryDTO user;
    private String roleCode;
    private String status;
    private ProjectSummaryDTO project; 
    private UserSummaryDTO invitedBy;
    private LocalDateTime invitationDate; 
    public ProjectUserDTO() {
    }

 

    public ProjectUserDTO(Integer id, UserSummaryDTO user, String roleCode, String status,
                          ProjectSummaryDTO project, UserSummaryDTO invitedBy, LocalDateTime invitationDate) {
        this.id = id;
        this.user = user;
        this.roleCode = roleCode;
        this.status = status;
        this.project = project;
        this.invitedBy = invitedBy;
        this.invitationDate = invitationDate;
    }


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public UserSummaryDTO getUser() { return user; }
    public void setUser(UserSummaryDTO user) { this.user = user; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public ProjectSummaryDTO getProject() { return project; }
    public void setProject(ProjectSummaryDTO project) { this.project = project; }
    public UserSummaryDTO getInvitedBy() { return invitedBy; }
    public void setInvitedBy(UserSummaryDTO invitedBy) { this.invitedBy = invitedBy; }
    public LocalDateTime getInvitationDate() { return invitationDate; }
    public void setInvitationDate(LocalDateTime invitationDate) { this.invitationDate = invitationDate; }
}