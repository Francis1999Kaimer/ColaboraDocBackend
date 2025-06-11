package com.example.project.enums;



public enum FolderType {
    REGULAR(1, "Carpeta Regular", "Carpeta normal para organizar documentos"),
    BIN(2, "Papelera", "Carpeta especial para elementos eliminados"),
    SYSTEM(3, "Sistema", "Carpeta del sistema (reservada)");

    private final Integer code;
    private final String name;
    private final String description;

    FolderType(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}
