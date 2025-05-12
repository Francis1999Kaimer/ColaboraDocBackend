package com.example.project.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DocumentCreateRequestDTO {
    @NotBlank(message = "El nombre del documento es obligatorio.")
    @Size(min = 1, max = 255, message = "El nombre debe tener entre 1 y 255 caracteres.")
    private String name;

    private String description;

    @NotNull(message = "El ID de la carpeta es obligatorio.")
    private Integer folderId;

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

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }
}