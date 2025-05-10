package com.example.project.DTO;

public class UserSummaryDTO {
    private Integer iduser;
    private String email;
    private String names;
    private String lastnames;

    // Constructores
    public UserSummaryDTO() {
    }

    public UserSummaryDTO(Integer iduser, String email, String names, String lastnames) {
        this.iduser = iduser;
        this.email = email;
        this.names = names;
        this.lastnames = lastnames;
    }

    // Getters y Setters
    public Integer getIduser() {
        return iduser;
    }

    public void setIduser(Integer iduser) {
        this.iduser = iduser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getLastnames() {
        return lastnames;
    }

    public void setLastnames(String lastnames) {
        this.lastnames = lastnames;
    }
}