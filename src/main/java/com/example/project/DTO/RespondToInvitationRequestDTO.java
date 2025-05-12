package com.example.project.DTO;

import jakarta.validation.constraints.NotNull;

public class RespondToInvitationRequestDTO {
    @NotNull(message = "La respuesta de aceptación es obligatoria.")
    private Boolean accept;

    public Boolean getAccept() {
        return accept;
    }

    public void setAccept(Boolean accept) { 
        this.accept = accept;
    }
}