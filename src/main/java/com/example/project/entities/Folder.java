package com.example.project.entities;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set; 

@Entity
@Table(name = "folders")
@EntityListeners(AuditingEntityListener.class)
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idfolder;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproject", nullable = true)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id", nullable = true)
    private Folder parentFolder;


    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Folder> childFolders = new HashSet<>();


    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Document> documents = new HashSet<>();

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false, updatable = false)
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Folder() {
    }

    public Integer getIdfolder() { return idfolder; }
    public void setIdfolder(Integer idfolder) { this.idfolder = idfolder; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public Folder getParentFolder() { return parentFolder; }
    public void setParentFolder(Folder parentFolder) { this.parentFolder = parentFolder; }
    
    public Set<Folder> getChildFolders() { return childFolders; }
    public void setChildFolders(Set<Folder> childFolders) { this.childFolders = childFolders; }
    public Set<Document> getDocuments() { return documents; }
    public void setDocuments(Set<Document> documents) { this.documents = documents; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void addChildFolder(Folder folder) {
        this.childFolders.add(folder);
        folder.setParentFolder(this);
    }
    public void removeChildFolder(Folder folder) {
        this.childFolders.remove(folder);
        folder.setParentFolder(null);
    }
    public void addDocument(Document document) {
        this.documents.add(document);
        document.setFolder(this);
    }
    public void removeDocument(Document document) {
        this.documents.remove(document);
        document.setFolder(null);
    }
}