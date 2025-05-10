package com.example.project.entities;

// import com.fasterxml.jackson.annotation.JsonBackReference; // Quitar o comentar si se usa DTO puro
import jakarta.persistence.*;

@Entity
@Table(name = "project_user")
public class ProjectUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iduser", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproject", nullable = false)
    // @JsonBackReference // Quitar o comentar si se usa DTO puro para este endpoint
    private Project project;

    @Column(nullable = false)
    private String roleCode;

    // Getters y setters (sin cambios)
    // ... (los mismos que tenías)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
}