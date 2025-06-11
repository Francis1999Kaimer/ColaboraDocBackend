package com.example.project.enums;



public enum Permission {
    
    DELETE_PROJECT("Eliminar proyecto"),
    CREATE_PROJECT("Crear proyecto"),
    EDIT_PROJECT("Editar proyecto"),
    VIEW_PROJECT("Ver proyecto"),
    RESTORE_PROJECT("Restaurar proyecto"),
    
    
    DELETE_FOLDER("Eliminar carpeta"),
    CREATE_FOLDER("Crear carpeta"),
    EDIT_FOLDER("Editar carpeta"),
    VIEW_FOLDER("Ver carpeta"),
    RESTORE_FOLDER("Restaurar carpeta"),
    
    
    DELETE_DOCUMENT("Eliminar documento"),
    CREATE_DOCUMENT("Crear documento"),
    EDIT_DOCUMENT("Editar documento"),
    VIEW_DOCUMENT("Ver documento"),
    RESTORE_DOCUMENT("Restaurar documento"),
    
    
    DELETE_VERSION("Eliminar versión"),
    CREATE_VERSION("Crear versión"),
    EDIT_VERSION("Editar versión"),
    VIEW_VERSION("Ver versión"),
    RESTORE_VERSION("Restaurar versión"),
    
    
    MANAGE_USERS("Gestionar usuarios"),
    INVITE_USERS("Invitar usuarios"),
    REMOVE_USERS("Remover usuarios"),
    MANAGE_USER_ROLES("Gestionar roles de usuarios"),
    VIEW_DELETED_ITEMS("Ver elementos eliminados");
    

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
