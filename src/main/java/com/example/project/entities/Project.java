package com.example.project.entities;

// import com.fasterxml.jackson.annotation.JsonManagedReference; // Quitar o comentar si se usa DTO puro
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idproject;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonManagedReference // Quitar o comentar si se usa DTO puro para este endpoint
    private List<ProjectUser> projectUsers;

    // Getters y setters (sin cambios)
    // ... (los mismos que tenías)
    public Integer getIdproject() { return idproject; }
    public void setIdproject(Integer idproject) { this.idproject = idproject; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<ProjectUser> getProjectUsers() { return projectUsers; }
    public void setProjectUsers(List<ProjectUser> projectUsers) { this.projectUsers = projectUsers; }
}