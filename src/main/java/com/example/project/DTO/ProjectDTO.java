package com.example.project.DTO;

// import java.util.List; // Descomentar si decides incluir ProjectUserDTOs

public class ProjectDTO {
    private Integer idproject;
    private String name;
    private String description;
    // private List<ProjectUserDTO> usersInProject; // Ejemplo si quisieras incluir usuarios

    // Constructores
    public ProjectDTO() {
    }

    public ProjectDTO(Integer idproject, String name, String description) {
        this.idproject = idproject;
        this.name = name;
        this.description = description;
    }

    // public ProjectDTO(Integer idproject, String name, String description, List<ProjectUserDTO> usersInProject) {
    //     this.idproject = idproject;
    //     this.name = name;
    //     this.description = description;
    //     this.usersInProject = usersInProject;
    // }


    // Getters y Setters
    public Integer getIdproject() {
        return idproject;
    }

    public void setIdproject(Integer idproject) {
        this.idproject = idproject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // public List<ProjectUserDTO> getUsersInProject() {
    //     return usersInProject;
    // }

    // public void setUsersInProject(List<ProjectUserDTO> usersInProject) {
    //     this.usersInProject = usersInProject;
    // }
}