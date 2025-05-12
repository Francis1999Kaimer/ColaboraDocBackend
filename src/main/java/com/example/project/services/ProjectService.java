package com.example.project.services;

import com.example.project.DTO.FolderDTO;
import com.example.project.DTO.ProjectDTO;
import com.example.project.DTO.ProjectRequest;
import com.example.project.DTO.ProjectUserDTO;
import com.example.project.DTO.ProjectUserInviteRequestDTO;
import com.example.project.entities.Project;
import com.example.project.entities.ProjectUser;
import com.example.project.entities.User;
import com.example.project.entities.Folder; 
import com.example.project.exception.ForbiddenAccessException;
import com.example.project.repositories.ProjectRepository;
import com.example.project.repositories.ProjectUserRepository;
import com.example.project.repositories.UserRepository;
import com.example.project.repositories.FolderRepository; 
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectUserRepository projectUserRepository;
    private final dtoMapper dtoMapper;
    private final EmailService emailService;
    private final FolderRepository folderRepository;

     @Autowired
    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
                          ProjectUserRepository projectUserRepository, dtoMapper dtoMapper,
                          EmailService emailService, FolderRepository folderRepository) { 
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectUserRepository = projectUserRepository;
        this.dtoMapper = dtoMapper;
        this.emailService = emailService;
        this.folderRepository = folderRepository;
    }

    @Transactional
    public ProjectDTO createProject(ProjectRequest request, User currentUser) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project savedProject = projectRepository.save(project);

        ProjectUser projectUser = new ProjectUser();
        projectUser.setUser(currentUser); 
        projectUser.setProject(savedProject);
        projectUser.setRoleCode("ADMIN"); 
        projectUser.setStatus(ProjectUser.InvitationStatus.ACCEPTED);
        projectUser.setActionDate(LocalDateTime.now());

        projectUserRepository.save(projectUser);

        logger.info("Proyecto '{}' creado por el usuario '{}'", savedProject.getName(), currentUser.getEmail());
        return dtoMapper.toProjectDTO(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsForUser(User user) {
        return projectUserRepository.findByUserAndStatus(user, ProjectUser.InvitationStatus.ACCEPTED).stream()
                .map(ProjectUser::getProject)
                .distinct()
                .map(dtoMapper::toProjectDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectUserDTO inviteUserToProject(ProjectUserInviteRequestDTO requestDTO, User invitingUser) {
        Project project = projectRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + requestDTO.getProjectId()));

       
        projectUserRepository.findByUserAndProjectAndStatusIn(
            invitingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED)
        )
        .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode())) 
        .orElseThrow(() -> {
            logger.warn("Usuario '{}' intentó invitar al proyecto '{}' sin permisos de ADMIN.",
                    invitingUser.getEmail(), project.getName());
            return new ForbiddenAccessException("No tienes permisos para invitar usuarios a este proyecto.");
        });

        User userToInvite = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario a invitar no encontrado con ID: " + requestDTO.getUserId()));

        if (projectUserRepository.findByUserAndProjectAndStatusIn(
                userToInvite, project, Arrays.asList(ProjectUser.InvitationStatus.PENDING, ProjectUser.InvitationStatus.ACCEPTED)
            ).isPresent()) {
            throw new IllegalArgumentException("El usuario " + userToInvite.getEmail() + " ya tiene una invitación pendiente o es miembro de este proyecto.");
        }


        ProjectUser newInvitation = new ProjectUser();
        newInvitation.setProject(project);
        newInvitation.setUser(userToInvite);
        newInvitation.setRoleCode(requestDTO.getRoleCode());
        newInvitation.setStatus(ProjectUser.InvitationStatus.PENDING);
    

        ProjectUser savedInvitation = projectUserRepository.save(newInvitation);
        logger.info("Invitación guardada ID {} para '{}' al proyecto '{}' por '{}'",
                savedInvitation.getId(), userToInvite.getEmail(), project.getName(), invitingUser.getEmail());

       
        try {
         
            emailService.sendProjectInvitationEmail(userToInvite, invitingUser, savedInvitation);
        } catch (Exception e) {
      
            logger.error("Error al intentar enviar el email de invitación para la invitación ID {}. La invitación fue creada.", savedInvitation.getId(), e);
        }
   

        return dtoMapper.toProjectUserDTO(savedInvitation);
    }

    @Transactional(readOnly = true)
    public List<ProjectUserDTO> getProjectMembers(Integer projectId, User requestingUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));

        projectUserRepository.findByUserAndProjectAndStatusIn(
                    requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED)
                )
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó acceder a miembros del proyecto '{}' sin ser miembro aceptado.",
                            requestingUser.getEmail(), project.getName());
                    return new ForbiddenAccessException("No tienes acceso para ver los miembros de este proyecto.");
                });

        return projectUserRepository.findByProject(project).stream()
                .filter(pu -> pu.getStatus() == ProjectUser.InvitationStatus.ACCEPTED)
                .map(dtoMapper::toProjectUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectUserDTO> getPendingInvitationsForUser(User user) {
        return projectUserRepository.findByUserAndStatus(user, ProjectUser.InvitationStatus.PENDING)
                .stream()
                .map(dtoMapper::toProjectUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectUserDTO respondToInvitation(Integer projectUserId, User userResponding, boolean accept) {
        ProjectUser invitation = projectUserRepository.findById(projectUserId)
                .orElseThrow(() -> new EntityNotFoundException("Invitación no encontrada con ID: " + projectUserId));

        if (!invitation.getUser().getIduser().equals(userResponding.getIduser())) {
            throw new ForbiddenAccessException("Esta invitación no te pertenece.");
        }
        if (invitation.getStatus() != ProjectUser.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Esta invitación ya ha sido respondida.");
        }

        invitation.setStatus(accept ? ProjectUser.InvitationStatus.ACCEPTED : ProjectUser.InvitationStatus.REJECTED);
        invitation.setActionDate(LocalDateTime.now());
     
        ProjectUser updatedInvitation = projectUserRepository.save(invitation);

        String action = accept ? "aceptada" : "rechazada";
        logger.info("Invitación ID {} para el proyecto '{}' {} por el usuario '{}'",
                invitation.getId(), invitation.getProject().getName(), action, userResponding.getEmail());

        return dtoMapper.toProjectUserDTO(updatedInvitation);
    }


    
    @Transactional(readOnly = true)
    public List<FolderDTO> getFoldersForProject(Integer projectId, User requestingUser) {
        logger.debug("Solicitando carpetas para el proyecto ID: {} por el usuario: {}", projectId, requestingUser.getEmail());
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.warn("Intento de acceso a carpetas de proyecto no existente: {}", projectId);
                    return new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId);
                });

        projectUserRepository.findByUserAndProjectAndStatusIn(
            requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED)
        ).orElseThrow(() -> {
            logger.warn("Acceso denegado: Usuario {} intentó acceder a carpetas del proyecto {} sin ser miembro aceptado.",
                requestingUser.getEmail(), projectId);
            return new ForbiddenAccessException("No tienes acceso a este proyecto.");
        });

 
        List<Folder> rootFolders = folderRepository.findByProjectAndParentFolderIsNull(project);
        logger.debug("Encontradas {} carpetas raíz para el proyecto ID: {}", rootFolders.size(), projectId);

        
        return rootFolders.stream()
                .map(dtoMapper::toFolderDTOWithHierarchy)
                .collect(Collectors.toList());
    }
}