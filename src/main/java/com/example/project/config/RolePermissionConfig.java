package com.example.project.config;

import com.example.project.enums.Permission;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;



@Component
public class RolePermissionConfig {
    
    

    private static final Map<String, Set<Permission>> ROLE_PERMISSIONS = Map.of(
        
        
        "ADMIN", EnumSet.of(
            
            Permission.DELETE_PROJECT, Permission.CREATE_PROJECT, 
            Permission.EDIT_PROJECT, Permission.VIEW_PROJECT, Permission.RESTORE_PROJECT,
            
            
            Permission.DELETE_FOLDER, Permission.CREATE_FOLDER, 
            Permission.EDIT_FOLDER, Permission.VIEW_FOLDER, Permission.RESTORE_FOLDER,
            
            
            Permission.DELETE_DOCUMENT, Permission.CREATE_DOCUMENT, 
            Permission.EDIT_DOCUMENT, Permission.VIEW_DOCUMENT, Permission.RESTORE_DOCUMENT,
            
            
            Permission.DELETE_VERSION, Permission.CREATE_VERSION, 
            Permission.EDIT_VERSION, Permission.VIEW_VERSION, Permission.RESTORE_VERSION,
            
            
            Permission.MANAGE_USERS, Permission.INVITE_USERS, 
            Permission.REMOVE_USERS, Permission.VIEW_DELETED_ITEMS,  Permission.MANAGE_USER_ROLES 
        ),
        
        
        "EDITOR", EnumSet.of(
            
            Permission.VIEW_PROJECT,
            
            
            Permission.DELETE_FOLDER, Permission.CREATE_FOLDER, 
            Permission.EDIT_FOLDER, Permission.VIEW_FOLDER,
            
            
            Permission.DELETE_DOCUMENT, Permission.CREATE_DOCUMENT, 
            Permission.EDIT_DOCUMENT, Permission.VIEW_DOCUMENT,
            
            
            Permission.DELETE_VERSION, Permission.CREATE_VERSION, 
            Permission.EDIT_VERSION, Permission.VIEW_VERSION
        ),
        
        
        "VIEWER", EnumSet.of(
            Permission.VIEW_PROJECT,
            Permission.VIEW_FOLDER,
            Permission.VIEW_DOCUMENT,
            Permission.VIEW_VERSION
        )
    );
    
    

    public boolean hasPermission(String roleCode, Permission permission) {
        if (roleCode == null || permission == null) {
            return false;
        }
        
        Set<Permission> permissions = ROLE_PERMISSIONS.get(roleCode.toUpperCase());
        return permissions != null && permissions.contains(permission);
    }
    
    

    public Set<Permission> getPermissions(String roleCode) {
        if (roleCode == null) {
            return EnumSet.noneOf(Permission.class);
        }
        
        return ROLE_PERMISSIONS.getOrDefault(roleCode.toUpperCase(), EnumSet.noneOf(Permission.class));
    }
    
    

    public Set<String> getAvailableRoles() {
        return ROLE_PERMISSIONS.keySet();
    }
    
    

    public boolean isValidRole(String roleCode) {
        return roleCode != null && ROLE_PERMISSIONS.containsKey(roleCode.toUpperCase());
    }
}
