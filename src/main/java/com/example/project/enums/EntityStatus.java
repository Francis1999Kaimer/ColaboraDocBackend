package com.example.project.enums;



public enum EntityStatus {
    ACTIVE(1, "Activo"),
    DELETED(2, "Eliminado"),
    ARCHIVED(3, "Archivado");

    private final Integer code;
    private final String description;

    EntityStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    

    public static EntityStatus fromCode(Integer code) {
        for (EntityStatus status : EntityStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código de estado no válido: " + code);
    }

    

    public boolean isActive() {
        return this == ACTIVE;
    }

    

    public boolean isDeleted() {
        return this == DELETED;
    }
}
