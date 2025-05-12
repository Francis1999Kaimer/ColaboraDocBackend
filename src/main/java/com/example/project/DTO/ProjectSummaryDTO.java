package com.example.project.DTO;

public class ProjectSummaryDTO {
    private Integer idproject;
    private String name;

    public ProjectSummaryDTO() {}

    public ProjectSummaryDTO(Integer idproject, String name) {
        this.idproject = idproject;
        this.name = name;
    }

    public Integer getIdproject() { return idproject; }
    public void setIdproject(Integer idproject) { this.idproject = idproject; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}