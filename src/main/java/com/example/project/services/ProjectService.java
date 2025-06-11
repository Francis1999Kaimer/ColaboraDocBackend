package com.example.project.services;

import com.example.project.DTO.ChangeUserRoleRequestDTO;
import com.example.project.DTO.FolderDTO;
import com.example.project.DTO.ProjectDTO;
import com.example.project.DTO.ProjectRequest;
import com.example.project.DTO.ProjectUserDTO;
import com.example.project.DTO.ProjectUserInviteRequestDTO;
import com.example.project.entities.Project;
import com.example.project.entities.ProjectUser;
import com.example.project.entities.User;
import com.example.project.entities.Folder;
import com.example.project.enums.FolderType;
import com.example.project.exception.ForbiddenAccessException;
import com.example.project.config.RolePermissionConfig;
import com.example.project.repositories.ProjectRepository;
import com.example.project.repositories.ProjectUserRepository;
import com.example.project.repositories.UserRepository;
import com.example.project.repositories.FolderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final SoftDeleteService softDeleteService;
    private final NotificationService notificationService;
    private final RolePermissionConfig rolePermissionConfig;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
            ProjectUserRepository projectUserRepository, dtoMapper dtoMapper,
            EmailService emailService, FolderRepository folderRepository,
            SoftDeleteService softDeleteService, NotificationService notificationService,
            RolePermissionConfig rolePermissionConfig) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectUserRepository = projectUserRepository;
        this.dtoMapper = dtoMapper;
        this.emailService = emailService;
        this.folderRepository = folderRepository;
        this.softDeleteService = softDeleteService;
        this.notificationService = notificationService;
        this.rolePermissionConfig = rolePermissionConfig;
    }

    @Transactional
    public ProjectDTO createProject(ProjectRequest request, User currentUser) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project savedProject = projectRepository.save(project);

        
        Folder binFolder = new Folder();
        binFolder.setName("Papelera");
        binFolder.setDescription("Carpeta de papelera del proyecto");
        binFolder.setProject(savedProject);
        binFolder.setFolderType(FolderType.BIN);
        binFolder.setParentFolder(null); 
        folderRepository.save(binFolder);

        ProjectUser projectUser = new ProjectUser();
        projectUser.setUser(currentUser);
        projectUser.setProject(savedProject);
        projectUser.setRoleCode("ADMIN");
        projectUser.setStatusInvitacion(ProjectUser.InvitationStatus.ACCEPTED);
        projectUser.setActionDate(LocalDateTime.now());

        projectUserRepository.save(projectUser);

        logger.info("Proyecto '{}' creado por el usuario '{}' con carpeta BIN automática", savedProject.getName(),
                currentUser.getEmail());
        return dtoMapper.toProjectDTO(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsForUser(User user) {
        return projectUserRepository.findActiveByUserAndStatusInvitacion(user, ProjectUser.InvitationStatus.ACCEPTED)
                .stream()
                .map(ProjectUser::getProject)
                .filter(project -> project.isActive()) 
                .distinct()
                .map(dtoMapper::toProjectDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectUserDTO inviteUserToProject(ProjectUserInviteRequestDTO requestDTO, User invitingUser) {
        Project project = projectRepository.findByIdAndActive(requestDTO.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Proyecto no encontrado con ID: " + requestDTO.getProjectId()));
        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                invitingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó invitar al proyecto '{}' sin permisos de ADMIN.",
                            invitingUser.getEmail(), project.getName());
                    return new ForbiddenAccessException("No tienes permisos para invitar usuarios a este proyecto.");
                });

        User userToInvite = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario a invitar no encontrado con ID: " + requestDTO.getUserId()));

        if (projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                userToInvite, project,
                Arrays.asList(ProjectUser.InvitationStatus.PENDING, ProjectUser.InvitationStatus.ACCEPTED))
                .isPresent()) {
            throw new IllegalArgumentException("El usuario " + userToInvite.getEmail()
                    + " ya tiene una invitación pendiente o es miembro de este proyecto.");
        }

        ProjectUser newInvitation = new ProjectUser();
        newInvitation.setProject(project);
        newInvitation.setUser(userToInvite);
        newInvitation.setRoleCode(requestDTO.getRoleCode());
        newInvitation.setStatusInvitacion(ProjectUser.InvitationStatus.PENDING);
        ProjectUser savedInvitation = projectUserRepository.save(newInvitation);
        logger.info("Invitación guardada ID {} para '{}' al proyecto '{}' por '{}'",
                savedInvitation.getId(), userToInvite.getEmail(), project.getName(), invitingUser.getEmail());

        
        try {
            String notificationMessage = String.format("Has sido invitado al proyecto '%s' como %s por %s %s",
                    project.getName(),
                    requestDTO.getRoleCode(),
                    invitingUser.getNames(),
                    invitingUser.getLastnames());

            notificationService.createNotification(
                    userToInvite,
                    notificationMessage,
                    project,
                    "PROJECT_INVITATION",
                    savedInvitation.getId(),
                    invitingUser);
        } catch (Exception e) {
            logger.error("Error al crear notificación para la invitación ID {}: {}", savedInvitation.getId(),
                    e.getMessage());
        }

        try {

            emailService.sendProjectInvitationEmail(userToInvite, invitingUser, savedInvitation);
        } catch (Exception e) {

            logger.error(
                    "Error al intentar enviar el email de invitación para la invitación ID {}. La invitación fue creada.",
                    savedInvitation.getId(), e);
        }

        return dtoMapper.toProjectUserDTO(savedInvitation);
    }

    @Transactional(readOnly = true)
    public List<ProjectUserDTO> getProjectMembers(Integer projectId, User requestingUser) {
        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));
        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó acceder a miembros del proyecto '{}' sin ser miembro aceptado.",
                            requestingUser.getEmail(), project.getName());
                    return new ForbiddenAccessException("No tienes acceso para ver los miembros de este proyecto.");
                });
        return projectUserRepository
                .findActiveByProjectAndStatusInvitacion(project, ProjectUser.InvitationStatus.ACCEPTED)
                .stream()
                .map(dtoMapper::toProjectUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectUserDTO> getPendingInvitationsForUser(User user) {
        return projectUserRepository.findByUserAndStatusInvitacion(user, ProjectUser.InvitationStatus.PENDING)
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
        if (invitation.getStatusInvitacion() != ProjectUser.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Esta invitación ya ha sido respondida.");
        }

        invitation.setStatusInvitacion(
                accept ? ProjectUser.InvitationStatus.ACCEPTED : ProjectUser.InvitationStatus.REJECTED);
        invitation.setActionDate(LocalDateTime.now());
        ProjectUser updatedInvitation = projectUserRepository.save(invitation);

        String action = accept ? "aceptada" : "rechazada";
        logger.info("Invitación ID {} para el proyecto '{}' {} por el usuario '{}'",
                invitation.getId(), invitation.getProject().getName(), action, userResponding.getEmail());

        
        
        if (invitation.getInvitedBy() != null
                && !invitation.getInvitedBy().getIduser().equals(userResponding.getIduser())) {
            try {
                String notificationMessage = String.format("%s %s ha %s tu invitación al proyecto '%s'",
                        userResponding.getNames(),
                        userResponding.getLastnames(),
                        action,
                        invitation.getProject().getName());

                notificationService.createNotification(
                        invitation.getInvitedBy(),
                        notificationMessage,
                        invitation.getProject(),
                        "INVITATION_RESPONSE",
                        invitation.getId(),
                        userResponding);
            } catch (Exception e) {
                logger.error("Error al crear notificación de respuesta a invitación ID {}: {}", invitation.getId(),
                        e.getMessage());
            }
        }

        return dtoMapper.toProjectUserDTO(updatedInvitation);
    }

    @Transactional(readOnly = true)
    public List<FolderDTO> getFoldersForProject(Integer projectId, User requestingUser) {

        logger.debug("Solicitando carpetas para el proyecto ID: {} por el usuario: {}", projectId,
                requestingUser.getEmail());

        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> {
                    logger.warn("Intento de acceso a carpetas de proyecto no existente: {}", projectId);
                    return new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId);
                });

        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED)).orElseThrow(() -> {
                    logger.warn(
                            "Acceso denegado: Usuario {} intentó acceder a carpetas del proyecto {} sin ser miembro aceptado.",
                            requestingUser.getEmail(), projectId);
                    return new ForbiddenAccessException("No tienes acceso a este proyecto.");
                });

        List<Folder> rootFolders = folderRepository.findActiveByProjectAndParentFolderIsNull(project);

        logger.debug("Encontradas {} carpetas raíz para el proyecto ID: {}", rootFolders.size(), projectId);

        return rootFolders.stream()
                .map(dtoMapper::toFolderDTOWithHierarchy)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Integer projectId, User requestingUser) {
        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));
        
        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED)).orElseThrow(() -> {
                    logger.warn("Acceso denegado: Usuario {} intentó acceder al proyecto {} sin ser miembro aceptado.",
                            requestingUser.getEmail(), projectId);
                    return new ForbiddenAccessException("No tienes acceso a este proyecto.");
                });

        return dtoMapper.toProjectDTO(project);
    }

    @Transactional
    public void deleteProject(Integer projectId, User requestingUser, String deletedBy) {
        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));
        
        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó eliminar el proyecto '{}' sin permisos de ADMIN.",
                            requestingUser.getEmail(), project.getName());
                    return new ForbiddenAccessException("No tienes permisos para eliminar este proyecto.");
                });
        softDeleteService.softDelete(project, deletedBy);
        projectRepository.save(project);

        
        
        try {
            List<ProjectUser> projectMembers = projectUserRepository
                    .findActiveByProjectAndStatusInvitacion(project, ProjectUser.InvitationStatus.ACCEPTED)
                    .stream()
                    .filter(member -> !member.getUser().getIduser().equals(requestingUser.getIduser()))
                    .collect(Collectors.toList());

            for (ProjectUser member : projectMembers) {
                String notificationMessage = String.format("El proyecto '%s' ha sido eliminado por %s %s",
                        project.getName(),
                        requestingUser.getNames(),
                        requestingUser.getLastnames());

                notificationService.createNotification(
                        member.getUser(),
                        notificationMessage,
                        project,
                        "PROJECT_DELETED",
                        projectId,
                        requestingUser);
            }
        } catch (Exception e) {
            logger.error("Error al crear notificaciones de eliminación del proyecto ID {}: {}", projectId,
                    e.getMessage());
        }

        
        try {
            notificationService.cleanupNotificationsForProject(project, deletedBy);
        } catch (Exception e) {
            logger.error("Error al limpiar notificaciones del proyecto ID {}: {}", projectId, e.getMessage());
        }

        logger.info("Proyecto '{}' eliminado (soft delete) por el usuario '{}'",
                project.getName(), requestingUser.getEmail());
    }

    @Transactional
    public void restoreProject(Integer projectId, User requestingUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));

        if (!project.isDeleted()) {
            throw new IllegalStateException("El proyecto con ID " + projectId + " no está eliminado");
        }
        
        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó restaurar el proyecto '{}' sin permisos de ADMIN.",
                            requestingUser.getEmail(), project.getName());
                    return new ForbiddenAccessException("No tienes permisos para restaurar este proyecto.");
                });

        project.restore();
        projectRepository.save(project);
        logger.info("Proyecto '{}' restaurado por el usuario '{}'",
                project.getName(), requestingUser.getEmail());
    }

    @Transactional
    public ProjectUserDTO changeUserRole(Integer projectId, ChangeUserRoleRequestDTO requestDTO, User requestingUser) {
        
        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId)); 
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó cambiar roles en el proyecto '{}' sin permisos de ADMIN.",
                            requestingUser.getEmail(), project.getName());
                    return new ForbiddenAccessException("No tienes permisos para cambiar roles en este proyecto.");
                });

        
        User targetUser = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Usuario no encontrado con ID: " + requestDTO.getUserId()));

        ProjectUser targetProjectUser = projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                targetUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .orElseThrow(() -> {
                    logger.warn("Intento de cambiar rol a usuario '{}' que no es miembro aceptado del proyecto '{}'.",
                            targetUser.getEmail(), project.getName());
                    return new EntityNotFoundException("El usuario no es miembro de este proyecto.");
                });

        
        String newRoleCode = requestDTO.getNewRoleCode().toUpperCase();
        if (!rolePermissionConfig.isValidRole(newRoleCode)) {
            throw new IllegalArgumentException("Rol inválido: " + newRoleCode);
        }

        
        
        if (targetUser.getIduser().equals(requestingUser.getIduser()) &&
                "ADMIN".equalsIgnoreCase(targetProjectUser.getRoleCode()) &&
                !"ADMIN".equalsIgnoreCase(newRoleCode)) {
            
            long adminCount = projectUserRepository.findByProject(project).stream()
                    .filter(pu -> pu.getStatusInvitacion() == ProjectUser.InvitationStatus.ACCEPTED)
                    .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                    .count();

            if (adminCount <= 1) {
                throw new IllegalArgumentException(
                        "No puedes remover el rol de administrador cuando eres el único administrador del proyecto.");
            }
        }

        
        String oldRoleCode = targetProjectUser.getRoleCode();
        logger.info("Cambiando rol del usuario '{}' en proyecto '{}' de '{}' a '{}' por el administrador '{}'",
                targetUser.getEmail(), project.getName(), oldRoleCode, newRoleCode, requestingUser.getEmail());

        
        targetProjectUser.setRoleCode(newRoleCode);
        targetProjectUser.setActionDate(LocalDateTime.now());
        ProjectUser updatedProjectUser = projectUserRepository.save(targetProjectUser);

        
        
        if (!targetUser.getIduser().equals(requestingUser.getIduser())) {
            try {
                String notificationMessage = String.format(
                        "Tu rol en el proyecto '%s' ha sido cambiado de %s a %s por %s %s",
                        project.getName(),
                        formatRoleName(oldRoleCode),
                        formatRoleName(newRoleCode),
                        requestingUser.getNames(),
                        requestingUser.getLastnames());

                notificationService.createNotification(
                        targetUser,
                        notificationMessage,
                        project,
                        "ROLE_CHANGED",
                        updatedProjectUser.getId(),
                        requestingUser);
            } catch (Exception e) {
                logger.error("Error al crear notificación de cambio de rol para el usuario ID {}: {}",
                        targetUser.getIduser(), e.getMessage());
            }
        }

        logger.info("Rol del usuario '{}' en proyecto '{}' cambiado exitosamente de '{}' a '{}'",
                targetUser.getEmail(), project.getName(), oldRoleCode, newRoleCode);

        return dtoMapper.toProjectUserDTO(updatedProjectUser);
    }

    

    private String formatRoleName(String roleCode) {
        if (roleCode == null)
            return "No especificado";
        return switch (roleCode.toUpperCase()) {
            case "ADMIN" -> "Administrador";
            case "EDITOR" -> "Editor";
            case "VIEWER" -> "Visualizador";
            default -> roleCode;
        };
    }

    @Transactional
    public void removeUserFromProject(Integer projectId, Integer userId, User requestingUser) {
        logger.info("Iniciando remoción del usuario ID {} del proyecto ID {} por usuario '{}'",
                userId, projectId, requestingUser.getEmail());

        
        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> {
                    logger.warn("Proyecto con ID {} no encontrado o está inactivo", projectId);
                    return new EntityNotFoundException("Proyecto no encontrado");
                }); 
        ProjectUser requestingProjectUser = projectUserRepository
                .findByUserAndProjectAndStatusInvitacionIn(
                        requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' no pertenece al proyecto ID {}",
                            requestingUser.getEmail(), projectId);
                    return new ForbiddenAccessException("No perteneces a este proyecto");
                });

        if (!"ADMIN".equalsIgnoreCase(requestingProjectUser.getRoleCode())) {
            logger.warn("Usuario '{}' no tiene permisos de ADMIN para remover usuarios del proyecto ID {}",
                    requestingUser.getEmail(), projectId);
            throw new ForbiddenAccessException("Solo los administradores pueden remover usuarios del proyecto");
        }

        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Usuario con ID {} no encontrado", userId);
                    return new EntityNotFoundException("Usuario no encontrado");
                }); 
        ProjectUser targetProjectUser = projectUserRepository
                .findByUserAndProjectAndStatusInvitacionIn(
                        targetUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' no pertenece al proyecto ID {}",
                            targetUser.getEmail(), projectId);
                    return new EntityNotFoundException("El usuario no pertenece a este proyecto");
                });

        
        long activeUserCount = projectUserRepository.findByProject(project).stream()
                .filter(pu -> pu.getStatusInvitacion() == ProjectUser.InvitationStatus.ACCEPTED)
                .count();

        if (activeUserCount <= 1) {
            logger.warn("Intento de remover al único usuario activo del proyecto ID {}", projectId);
            throw new IllegalArgumentException(
                    "No se puede remover al único miembro activo del proyecto. Primero invita a otros usuarios al proyecto o elimina el proyecto.");
        }

        
        if ("ADMIN".equalsIgnoreCase(targetProjectUser.getRoleCode())) {
            long adminCount = projectUserRepository.findByProject(project).stream()
                    .filter(pu -> pu.getStatusInvitacion() == ProjectUser.InvitationStatus.ACCEPTED)
                    .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                    .count();

            if (adminCount <= 1) {
                logger.warn("Intento de remover al último ADMIN del proyecto ID {}", projectId);
                throw new IllegalArgumentException("No se puede remover al último administrador del proyecto");
            }
        }

        
        try {
            targetProjectUser.softDelete();
            projectUserRepository.save(targetProjectUser);

            logger.info("Usuario '{}' removido exitosamente del proyecto '{}' por '{}'",
                    targetUser.getEmail(), project.getName(), requestingUser.getEmail());

            
            try {
                String notificationMessage = String.format(
                        "Has sido removido del proyecto '%s' por el administrador %s %s",
                        project.getName(),
                        requestingUser.getNames() != null ? requestingUser.getNames() : "",
                        requestingUser.getLastnames() != null ? requestingUser.getLastnames() : "").trim();

                notificationService.createNotification(
                        targetUser,
                        notificationMessage,
                        project,
                        "USER_REMOVED",
                        targetProjectUser.getId(),
                        requestingUser);
            } catch (Exception e) {
                logger.error("Error al crear notificación de remoción para el usuario ID {}: {}",
                        targetUser.getIduser(), e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error al remover usuario '{}' del proyecto ID {}: {}",
                    targetUser.getEmail(), projectId, e.getMessage());
            throw new RuntimeException("Error al remover usuario del proyecto", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getDeletedProjectsForUser(User user) {
        return projectUserRepository.findActiveByUserAndStatusInvitacion(user, ProjectUser.InvitationStatus.ACCEPTED)
                .stream()
                .map(ProjectUser::getProject)
                .filter(project -> project.isDeleted())
                .distinct()
                .map(dtoMapper::toProjectDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional(readOnly = true)
    public List<ProjectUserDTO> getDeletedUsersFromProject(Integer projectId, User requestingUser) {
        
        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));

        
        
        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó ver usuarios eliminados del proyecto '{}' sin permisos de ADMIN.",
                            requestingUser.getEmail(), project.getName());
                    return new ForbiddenAccessException(
                            "No tienes permisos para ver usuarios eliminados de este proyecto.");
                });

        
        return projectUserRepository.findDeletedUsersByProject(project)
                .stream()
                .map(dtoMapper::toProjectUserDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional
    public void restoreUserToProject(Integer projectId, Integer userId, User requestingUser) {
        logger.info("Iniciando restauración del usuario ID {} al proyecto ID {} por usuario '{}'",
                userId, projectId, requestingUser.getEmail());

        
        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId));

        
        
        projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                requestingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó restaurar usuario en el proyecto '{}' sin permisos de ADMIN.",
                            requestingUser.getEmail(), project.getName());
                    return new ForbiddenAccessException("No tienes permisos para restaurar usuarios en este proyecto.");
                });

        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Usuario con ID {} no encontrado", userId);
                    return new EntityNotFoundException("Usuario no encontrado");
                });

        
        ProjectUser deletedProjectUser = projectUserRepository.findDeletedUserByUserIdAndProject(userId, project)
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' no tiene un registro eliminado en el proyecto ID {}",
                            targetUser.getEmail(), projectId);
                    return new EntityNotFoundException("El usuario no tiene un registro eliminado en este proyecto");
                });

        try {
            
            deletedProjectUser.restore();
            deletedProjectUser.setActionDate(LocalDateTime.now());
            projectUserRepository.save(deletedProjectUser);

            logger.info("Usuario '{}' restaurado exitosamente en el proyecto '{}' por '{}'",
                    targetUser.getEmail(), project.getName(), requestingUser.getEmail());

            
            
            try {
                String notificationMessage = String.format(
                        "Has sido restaurado automáticamente al proyecto '%s' por %s %s. Si no deseas participar en este proyecto, puedes salirte usando la opción 'Salir del proyecto' en el menú del proyecto.",
                        project.getName(),
                        requestingUser.getNames() != null ? requestingUser.getNames() : "",
                        requestingUser.getLastnames() != null ? requestingUser.getLastnames() : "").trim();

                notificationService.createNotification(
                        targetUser,
                        notificationMessage,
                        project,
                        "USER_RESTORED",
                        deletedProjectUser.getId(),
                        requestingUser);

                logger.info("Notificación de restauración creada para usuario '{}' del proyecto '{}'",
                        targetUser.getEmail(), project.getName());

            } catch (Exception e) {
                logger.error("Error al crear notificación de restauración para el usuario ID {}: {}",
                        targetUser.getIduser(), e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error al restaurar usuario '{}' en el proyecto ID {}: {}",
                    targetUser.getEmail(), projectId, e.getMessage());
            throw new RuntimeException("Error al restaurar usuario en el proyecto", e);
        }
    }

    

    @Transactional
    public void leaveProject(Integer projectId, User leavingUser) {
        logger.info("Usuario '{}' solicitando salirse del proyecto ID {}",
                leavingUser.getEmail(), projectId);

        
        Project project = projectRepository.findByIdAndActive(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado con ID: " + projectId)); 
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
                                                                                                                
        ProjectUser userProjectUser = projectUserRepository.findByUserAndProjectAndStatusInvitacionIn(
                leavingUser, project, Arrays.asList(ProjectUser.InvitationStatus.ACCEPTED))
                .orElseThrow(() -> {
                    logger.warn("Usuario '{}' intentó salirse del proyecto ID {} sin ser miembro",
                            leavingUser.getEmail(), projectId);
                    return new ForbiddenAccessException("No eres miembro de este proyecto");
                });

        
        long activeUserCount = projectUserRepository.findByProject(project).stream()
                .filter(pu -> pu.getStatusInvitacion() == ProjectUser.InvitationStatus.ACCEPTED)
                .count();

        if (activeUserCount <= 1) {
            logger.warn("Usuario '{}' intentó salirse siendo el único miembro activo del proyecto ID {}",
                    leavingUser.getEmail(), projectId);
            throw new IllegalArgumentException(
                    "No puedes salirte del proyecto siendo el único miembro activo. Primero invita a otros usuarios al proyecto o elimina el proyecto.");
        }

        
        if ("ADMIN".equalsIgnoreCase(userProjectUser.getRoleCode())) {
            long adminCount = projectUserRepository.findByProject(project).stream()
                    .filter(pu -> pu.getStatusInvitacion() == ProjectUser.InvitationStatus.ACCEPTED)
                    .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                    .count();

            if (adminCount <= 1) {
                logger.warn("Usuario '{}' intentó salirse siendo el último ADMIN del proyecto ID {}",
                        leavingUser.getEmail(), projectId);
                throw new IllegalArgumentException(
                        "No puedes salirte del proyecto siendo el único administrador. Primero asigna otro administrador o transfiere la propiedad del proyecto.");
            }
        }

        try {
            
            userProjectUser.softDelete();
            userProjectUser.setActionDate(LocalDateTime.now());
            projectUserRepository.save(userProjectUser);
            logger.info("Usuario '{}' se ha salido exitosamente del proyecto '{}'",
                    leavingUser.getEmail(), project.getName());

            
            try {
                List<ProjectUser> admins = projectUserRepository
                        .findActiveByProjectAndStatusInvitacion(project, ProjectUser.InvitationStatus.ACCEPTED)
                        .stream()
                        .filter(pu -> "ADMIN".equalsIgnoreCase(pu.getRoleCode()))
                        .filter(pu -> !pu.getUser().getIduser().equals(leavingUser.getIduser()))
                        .collect(Collectors.toList());

                for (ProjectUser admin : admins) {
                    String notificationMessage = String.format(
                            "%s %s ha abandonado voluntariamente el proyecto '%s'",
                            leavingUser.getNames() != null ? leavingUser.getNames() : "",
                            leavingUser.getLastnames() != null ? leavingUser.getLastnames() : "",
                            project.getName()).trim();

                    notificationService.createNotification(
                            admin.getUser(),
                            notificationMessage,
                            project,
                            "USER_LEFT",
                            userProjectUser.getId(),
                            leavingUser);
                }
                logger.info("Notificaciones de salida voluntaria enviadas a administradores del proyecto '{}'",
                        project.getName());

            } catch (Exception e) {
                logger.error("Error al crear notificaciones de salida voluntaria para el proyecto ID {}: {}",
                        projectId, e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error al procesar salida del usuario '{}' del proyecto ID {}: {}",
                    leavingUser.getEmail(), projectId, e.getMessage());
            throw new RuntimeException("Error al salirse del proyecto", e);
        }
    }
}