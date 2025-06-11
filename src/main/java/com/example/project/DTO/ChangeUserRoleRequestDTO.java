package com.example.project.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class ChangeUserRoleRequestDTO {
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Integer userId;
    
    @NotNull(message = "New role code is required")
    @Pattern(regexp = "^(ADMIN|EDITOR|VIEWER)$", 
             message = "Role code must be one of: ADMIN, EDITOR, VIEWER")
    private String newRoleCode;

    
    public ChangeUserRoleRequestDTO() {}

    
    public ChangeUserRoleRequestDTO(Integer userId, String newRoleCode) {
        this.userId = userId;
        this.newRoleCode = newRoleCode;
    }

    
    public Integer getUserId() {
        return userId;
    }

    public String getNewRoleCode() {
        return newRoleCode;
    }

    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setNewRoleCode(String newRoleCode) {
        this.newRoleCode = newRoleCode;
    }

    @Override
    public String toString() {
        return "ChangeUserRoleRequestDTO{" +
                "userId=" + userId +
                ", newRoleCode='" + newRoleCode + '\'' +
                '}';
    }
}
