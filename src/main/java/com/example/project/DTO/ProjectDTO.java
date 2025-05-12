package com.example.project.DTO;



public class ProjectDTO {
    private Integer idproject;
    private String name;
    private String description;

    public ProjectDTO() {
    }

    public ProjectDTO(Integer idproject, String name, String description) {
        this.idproject = idproject;
        this.name = name;
        this.description = description;
    }




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


}