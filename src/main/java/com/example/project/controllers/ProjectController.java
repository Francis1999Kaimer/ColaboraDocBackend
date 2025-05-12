package com.example.project.controllers;

import com.example.project.DTO.FolderDTO;
import com.example.project.DTO.ProjectDTO;
import com.example.project.DTO.ProjectRequest;
import com.example.project.DTO.ProjectUserDTO;
import com.example.project.DTO.ProjectUserInviteRequestDTO;
import com.example.project.DTO.RespondToInvitationRequestDTO;
import com.example.project.entities.User;
import com.example.project.repositories.UserRepository;
import com.example.project.services.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
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

}