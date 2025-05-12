package com.example.project.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FolderCreateRequestDTO {
    @NotBlank(message = "El nombre de la carpeta es obligatorio.")
    @Size(min = 1, max = 255, message = "El nombre debe tener entre 1 y 255 caracteres.")
    private String name;

    private String description;

    private Integer projectId;
    private Integer parentFolderId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Integer parentFolderId) {
        this.parentFolderId = parentFolderId;
    }
}