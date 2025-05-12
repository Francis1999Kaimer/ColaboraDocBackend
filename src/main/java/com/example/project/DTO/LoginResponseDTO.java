package com.example.project.DTO;

public class LoginResponseDTO {
    private Integer iduser;
    private String email;
    private String names;
    private String lastnames;
    

    public LoginResponseDTO(Integer iduser, String email, String names, String lastnames) {
        this.iduser = iduser;
        this.email = email;
        this.names = names;
        this.lastnames = lastnames;
    }

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