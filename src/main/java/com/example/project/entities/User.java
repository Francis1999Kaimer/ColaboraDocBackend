package com.example.project.entities;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer iduser;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String names;
    
    @Column(nullable = false)
    private String lastnames;

    @Column(nullable = false)
    private String userpassword;



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

    public String getUserpassword() {
        return userpassword;
    }

    public void setUserpassword(String userpassword) {
        this.userpassword = userpassword;
    }


    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names =  names;
    }



    public String getLastnames() {
        return lastnames;
    }

    public void setLasnames(String lastnames) {
        this.lastnames = lastnames;
    }

}
