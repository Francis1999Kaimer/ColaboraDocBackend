package com.example.project.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProjectUserInviteRequestDTO {
    @NotNull(message = "El ID del proyecto es obligatorio.")
    private Integer projectId;

    @NotNull(message = "El ID del usuario a invitar es obligatorio.") 
    private Integer userId;

    @NotBlank(message = "El código de rol es obligatorio.")
    private String roleCode;

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public Integer getUserId() { return userId; } 
    public void setUserId(Integer userId) { this.userId = userId; } 
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
}