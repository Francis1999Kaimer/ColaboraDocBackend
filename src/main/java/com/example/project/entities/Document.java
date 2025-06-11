package com.example.project.entities;

import com.example.project.entities.base.AuditableEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
public class Document extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer iddocument;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfolder", nullable = false)
    private Folder folder;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Version> versions = new ArrayList<>();
    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false, updatable = false)
    private User createdBy;

    public Document() {
        super(); 
        this.versions = new ArrayList<>();
    }


    public Integer getIddocument() { return iddocument; }
    public void setIddocument(Integer iddocument) { this.iddocument = iddocument; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }
    public List<Version> getVersions() { return versions; }
    public void setVersions(List<Version> versions) { this.versions = versions; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public void addVersion(Version version) {
        versions.add(version);
        version.setDocument(this);
    }

    public void removeVersion(Version version) {
        versions.remove(version);
        version.setDocument(null);
    }
}