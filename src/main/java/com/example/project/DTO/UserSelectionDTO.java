package com.example.project.DTO;

public class UserSelectionDTO {
    private Integer iduser;
    private String fullName;
    private String email;

    public UserSelectionDTO() {
    }

    public UserSelectionDTO(Integer iduser, String fullName, String email) {
        this.iduser = iduser;
        this.fullName = fullName;
        this.email = email;
    }


    public Integer getIduser() {
        return iduser;
    }

    public void setIduser(Integer iduser) {
        this.iduser = iduser;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}