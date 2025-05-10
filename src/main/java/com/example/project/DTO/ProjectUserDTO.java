package com.example.project.DTO;

public class ProjectUserDTO {
    private Integer id; // ID de la relación ProjectUser
    private UserSummaryDTO user;
    private String roleCode;

    // Constructores
    public ProjectUserDTO() {
    }

    public ProjectUserDTO(Integer id, UserSummaryDTO user, String roleCode) {
        this.id = id;
        this.user = user;
        this.roleCode = roleCode;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserSummaryDTO getUser() {
        return user;
    }

    public void setUser(UserSummaryDTO user) {
        this.user = user;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
}