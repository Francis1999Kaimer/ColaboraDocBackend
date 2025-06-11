package com.example.project.controllers;

import com.example.project.DTO.ChangeUserRoleRequestDTO;
import com.example.project.DTO.FolderDTO;
import com.example.project.DTO.ProjectDTO;
import com.example.project.DTO.ProjectRequest;
import com.example.project.DTO.ProjectUserDTO;
import com.example.project.DTO.ProjectUserInviteRequestDTO;
import com.example.project.DTO.RespondToInvitationRequestDTO;
import com.example.project.annotation.RequiresPermission;
import com.example.project.entities.User;
import com.example.project.enums.Permission;
import com.example.project.exception.ForbiddenAccessException;
import com.example.project.repositories.UserRepository;
import com.example.project.services.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;
    private final UserRepository userRepository;
    

    public ProjectController(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
        
            logger.error("UserDetails es null en getCurrentUser. El filtro JWT podría no estar funcionando correctamente o el endpoint no está asegurado.");
            throw new RuntimeException("No se pudo determinar el usuario autenticado.");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    logger.error("Usuario autenticado con email '{}' no encontrado en la base de datos.", userDetails.getUsername());
                    return new RuntimeException("Usuario autenticado no encontrado en la base de datos. Esto no debería ocurrir.");
                });
    }    
    @PostMapping("/create")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        ProjectDTO createdProject = projectService.createProject(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }


    @GetMapping("/list")
    public ResponseEntity<List<ProjectDTO>> getUserProjects(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        List<ProjectDTO> projectDTOs = projectService.getProjectsForUser(currentUser);
        return ResponseEntity.ok(projectDTOs);
    }    
    
    @PostMapping("/invite")
    @RequiresPermission(Permission.INVITE_USERS)
    public ResponseEntity<ProjectUserDTO> inviteUserToProject(@Valid @RequestBody ProjectUserInviteRequestDTO requestDTO,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        User invitingUser = getCurrentUser(userDetails);
        ProjectUserDTO projectUserDTO = projectService.inviteUserToProject(requestDTO, invitingUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectUserDTO);
    }

  
    @GetMapping("/{projectId}/users")
    public ResponseEntity<List<ProjectUserDTO>> getProjectMembers(@PathVariable Integer projectId,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User requestingUser = getCurrentUser(userDetails);
        List<ProjectUserDTO> users = projectService.getProjectMembers(projectId, requestingUser);
        return ResponseEntity.ok(users);
    }

    

  
    @GetMapping("/{projectId}/me")
    public ResponseEntity<String> getInfoUser(@PathVariable Integer projectId,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User requestingUser = getCurrentUser(userDetails);
        Integer userId = requestingUser.getIduser();
        List<ProjectUserDTO> users = projectService.getProjectMembers(projectId, requestingUser);
              
        for (ProjectUserDTO projectUserDTO : users) {
            if(projectUserDTO.getUser().getIduser() == userId) {
                String roleCode = projectUserDTO.getRoleCode();

                logger.error(roleCode);

                return ResponseEntity.ok(roleCode);
            }
        }
        return ResponseEntity.ok("VIEWER");
       
    }



    @GetMapping("/invitations/pending")
    public ResponseEntity<List<ProjectUserDTO>> getMyPendingInvitations(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        logger.info("Usuario '{}' solicitando sus invitaciones pendientes.", currentUser.getEmail());
        List<ProjectUserDTO> invitations = projectService.getPendingInvitationsForUser(currentUser);
        return ResponseEntity.ok(invitations);
    }

    @PostMapping("/invitations/{projectUserId}/respond")
    public ResponseEntity<ProjectUserDTO> respondToInvitation(
            @PathVariable Integer projectUserId,
            @Valid @RequestBody RespondToInvitationRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        logger.info("Usuario '{}' respondiendo a la invitación ID: {}. Aceptar: {}",
                currentUser.getEmail(), projectUserId, request.getAccept());
        ProjectUserDTO updatedInvitation = projectService.respondToInvitation(projectUserId, currentUser, request.getAccept());
        return ResponseEntity.ok(updatedInvitation);
    }

    @GetMapping("/{projectId}/folders")
    public ResponseEntity<List<FolderDTO>> getProjectFolders(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        List<FolderDTO> folderHierarchy = projectService.getFoldersForProject(projectId, currentUser);
        return ResponseEntity.ok(folderHierarchy);
    }
    
  
    
    @DeleteMapping("/delete/{projectId}")
    @RequiresPermission(Permission.DELETE_PROJECT)
    public ResponseEntity<Void> deleteProject(
            @PathVariable Integer projectId,
            @RequestParam(value = "deletedBy", required = false) String deletedBy,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        try {
            projectService.deleteProject(projectId, currentUser, deletedBy != null ? deletedBy : currentUser.getEmail());
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error al eliminar proyecto {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{projectId}/restore")
    @RequiresPermission(Permission.DELETE_PROJECT)
    public ResponseEntity<Void> restoreProject(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        try {
            projectService.restoreProject(projectId, currentUser);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error al restaurar proyecto {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }    }


    @GetMapping("/deleted")
    public ResponseEntity<List<ProjectDTO>> getDeletedProjects(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        try {
            List<ProjectDTO> deletedProjects = projectService.getDeletedProjectsForUser(currentUser);
            return ResponseEntity.ok(deletedProjects);
        } catch (Exception e) {
            logger.error("Error al obtener proyectos eliminados para usuario {}: {}", currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/{projectId}/users/{userId}/role")
    @RequiresPermission(Permission.MANAGE_USER_ROLES)
    public ResponseEntity<?> changeUserRole(
            @PathVariable Integer projectId,
            @PathVariable Integer userId,
            @Valid @RequestBody ChangeUserRoleRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        
        try {
            if (!userId.equals(requestDTO.getUserId())) {
                return ResponseEntity.badRequest()
                    .body("El ID de usuario en la URL no coincide con el ID en el cuerpo de la solicitud");
            }
            
            ProjectUserDTO updatedUser = projectService.changeUserRole(projectId, requestDTO, currentUser);
            return ResponseEntity.ok(updatedUser);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para cambiar roles en este proyecto");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al cambiar rol del usuario {} en proyecto {}: {}", 
                userId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }



    @DeleteMapping("/{projectId}/users/{userId}")
    @RequiresPermission(Permission.REMOVE_USERS)
    public ResponseEntity<?> removeUserFromProject(
            @PathVariable Integer projectId,
            @PathVariable Integer userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        
        try {
            projectService.removeUserFromProject(projectId, userId, currentUser);
            return ResponseEntity.noContent().build();
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para remover usuarios de este proyecto");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {            logger.error("Error al remover usuario {} del proyecto {}: {}", 
                userId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }

    @GetMapping("/{projectId}/users/deleted")
    @RequiresPermission(Permission.MANAGE_USER_ROLES)
    public ResponseEntity<?> getDeletedUsersFromProject(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        
        try {
            List<ProjectUserDTO> deletedUsers = projectService.getDeletedUsersFromProject(projectId, currentUser);
            return ResponseEntity.ok(deletedUsers);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para ver usuarios eliminados de este proyecto");
        } catch (Exception e) {
            logger.error("Error al obtener usuarios eliminados del proyecto {}: {}", 
                projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }

    @PostMapping("/{projectId}/users/{userId}/restore")
    @RequiresPermission(Permission.MANAGE_USER_ROLES)
    public ResponseEntity<?> restoreUserToProject(
            @PathVariable Integer projectId,
            @PathVariable Integer userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        
        try {
            projectService.restoreUserToProject(projectId, userId, currentUser);
            return ResponseEntity.ok().build();
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permisos para restaurar usuarios en este proyecto");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al restaurar usuario {} en el proyecto {}: {}", 
                userId, projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }

    @PostMapping("/{projectId}/leave")
    public ResponseEntity<?> leaveProject(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        
        try {
            logger.info("Usuario '{}' intentando salir del proyecto {}", currentUser.getEmail(), projectId);
            projectService.leaveProject(projectId, currentUser);
            logger.info("Usuario '{}' salió exitosamente del proyecto {}", currentUser.getEmail(), projectId);
            return ResponseEntity.ok().build();
            
        } catch (EntityNotFoundException e) {
            logger.warn("Proyecto {} no encontrado para usuario {}", projectId, currentUser.getEmail());
            return ResponseEntity.notFound().build();
        } catch (ForbiddenAccessException e) {
            logger.warn("Usuario {} sin permisos para salir del proyecto {}: {}", 
                currentUser.getEmail(), projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No puedes salir de este proyecto: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al salir del proyecto {}: {}", projectId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al salir del proyecto {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }

}