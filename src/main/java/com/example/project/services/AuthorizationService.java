package com.example.project.services;

import com.example.project.config.RolePermissionConfig;
import com.example.project.entities.ProjectUser;
import com.example.project.enums.Permission;
import com.example.project.repositories.ProjectUserRepository;
import com.example.project.repositories.FolderRepository;
import com.example.project.repositories.DocumentRepository;
import com.example.project.repositories.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;



@Service
public class AuthorizationService {
      @Autowired
    private RolePermissionConfig rolePermissionConfig;
    
    @Autowired
    private ProjectUserRepository projectUserRepository;
    
    @Autowired
    private FolderRepository folderRepository;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private VersionRepository versionRepository;
      

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }
    
    

    public String getCurrentUserEmail() {
        return getCurrentUsername();
    }
    
    

    public void checkPermission(Integer projectId, Permission permission) {
        String currentUser = getCurrentUsername();
        checkPermission(currentUser, projectId, permission);
    }
    
    

    public void checkPermission(String username, Integer projectId, Permission permission) {
        if (!hasPermission(username, projectId, permission)) {
            String userRole = getUserRoleInProject(username, projectId);
            throw new AccessDeniedException(
                String.format("Usuario '%s' con rol '%s' no tiene permiso '%s' en proyecto %d", 
                    username, userRole, permission.getDescription(), projectId)
            );
        }
    }
      

    public boolean hasPermission(Integer projectId, Permission permission) {
        String currentUser = getCurrentUsername();
        return hasPermission(currentUser, projectId, permission);
    }
    
    

    public boolean hasPermission(String username, Integer projectId, Permission permission) {
        try {
            String userRole = getUserRoleInProject(username, projectId);
            return rolePermissionConfig.hasPermission(userRole, permission);
        } catch (Exception e) {
            return false;
        }
    }
    
    

    public String getUserRoleInProject(String username, Integer projectId) {
        if (username == null || projectId == null) {
            return "VIEWER"; 
        }
        
        Optional<ProjectUser> projectUser = projectUserRepository
            .findByUserEmailAndProjectIdAndStatusInvitacion(username, projectId, ProjectUser.InvitationStatus.ACCEPTED);
        
        return projectUser
            .map(ProjectUser::getRoleCode)
            .orElse("VIEWER"); 
    }
    
    

    public Set<Permission> getUserPermissions(String username, Integer projectId) {
        String userRole = getUserRoleInProject(username, projectId);
        return rolePermissionConfig.getPermissions(userRole);
    }
      

    public Set<Permission> getCurrentUserPermissions(Integer projectId) {
        String currentUser = getCurrentUsername();
        return getUserPermissions(currentUser, projectId);
    }
    
    

    public boolean isAdmin(String username, Integer projectId) {
        return "ADMIN".equals(getUserRoleInProject(username, projectId));
    }
      

    public boolean isCurrentUserAdmin(Integer projectId) {
        String currentUser = getCurrentUsername();
        return isAdmin(currentUser, projectId);
    }
    
    

    public Integer getProjectIdFromFolder(Integer folderId) {
        return folderRepository.findById(folderId)
            .map(folder -> folder.getProject().getIdproject())
            .orElse(null);
    }
      

    public Integer getProjectIdFromDocument(Integer documentId) {
        return documentRepository.findById(documentId)
            .map(document -> document.getFolder().getProject().getIdproject())
            .orElse(null);
    }
    
    

    public Integer getProjectIdFromVersion(Integer versionId) {
        return versionRepository.findById(versionId)
            .map(version -> version.getDocument().getFolder().getProject().getIdproject())
            .orElse(null);
    }
    
    

    public void checkFolderPermission(Integer folderId, Permission permission) {
        Integer projectId = getProjectIdFromFolder(folderId);
        if (projectId == null) {
            throw new AccessDeniedException("No se pudo determinar el proyecto para la carpeta con ID: " + folderId);
        }
        checkPermission(projectId, permission);
    }
    
    

    public void checkDocumentPermission(Integer documentId, Permission permission) {
        Integer projectId = getProjectIdFromDocument(documentId);
        if (projectId == null) {
            throw new AccessDeniedException("No se pudo determinar el proyecto para el documento con ID: " + documentId);
        }
        checkPermission(projectId, permission);
    }
    
    

    public void checkVersionPermission(Integer versionId, Permission permission) {
        Integer projectId = getProjectIdFromVersion(versionId);
        if (projectId == null) {
            throw new AccessDeniedException("No se pudo determinar el proyecto para la versión con ID: " + versionId);
        }
        checkPermission(projectId, permission);
    }
}
