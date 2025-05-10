package com.example.project.controllers;

import com.example.project.DTO.ProjectDTO; // Importar el DTO
import com.example.project.DTO.ProjectRequest;
// import com.example.project.DTO.ProjectUserDTO; // Si decides usarlo en ProjectDTO
// import com.example.project.DTO.UserSummaryDTO; // Si decides usarlo en ProjectDTO
import com.example.project.entities.Project;
import com.example.project.entities.ProjectUser;
import com.example.project.entities.User;
import com.example.project.repositories.ProjectRepository;
import com.example.project.repositories.ProjectUserRepository;
import com.example.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createProject(@RequestBody ProjectRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        // No es necesario setear projectUsers aquí al crear un proyecto nuevo de esta forma.
        // La relación se establecerá a través de ProjectUser.
        projectRepository.save(project); // Guardar primero el proyecto para obtener su ID

        ProjectUser projectUser = new ProjectUser();
        projectUser.setUser(user);
        projectUser.setProject(project);
        projectUser.setRoleCode("ADMIN");
        projectUserRepository.save(projectUser);

        // Opcional: devolver el proyecto creado como DTO
        // ProjectDTO createdProjectDTO = new ProjectDTO(project.getIdproject(), project.getName(), project.getDescription());
        // return ResponseEntity.ok(createdProjectDTO);
        return ResponseEntity.ok("Proyecto creado exitosamente");
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getUserProjects(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<ProjectUser> projectUsers = projectUserRepository.findByUser(user);

        List<ProjectDTO> projectDTOs = projectUsers.stream()
                .map(ProjectUser::getProject) // Obtenemos la entidad Project
                .distinct() // En caso de que un usuario tenga múltiples roles en el mismo proyecto
                .map(projectEntity -> {
                    // Mapeo manual a ProjectDTO
                    ProjectDTO dto = new ProjectDTO();
                    dto.setIdproject(projectEntity.getIdproject());
                    dto.setName(projectEntity.getName());
                    dto.setDescription(projectEntity.getDescription());

                    // Si decidiste incluir la lista de ProjectUserDTO en ProjectDTO:
                    /*
                    List<ProjectUserDTO> usersInProjectDTO = projectEntity.getProjectUsers().stream()
                        .map(puEntity -> {
                            User uEntity = puEntity.getUser();
                            UserSummaryDTO userSummary = new UserSummaryDTO(
                                uEntity.getIduser(),
                                uEntity.getEmail(),
                                uEntity.getNames(),
                                uEntity.getLastnames()
                            );
                            return new ProjectUserDTO(puEntity.getId(), userSummary, puEntity.getRoleCode());
                        })
                        .collect(Collectors.toList());
                    dto.setUsersInProject(usersInProjectDTO);
                    */
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDTOs);
    }
}