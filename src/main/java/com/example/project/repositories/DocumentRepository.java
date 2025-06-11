package com.example.project.repositories;

import com.example.project.entities.Document;
import com.example.project.entities.Project;
import com.example.project.repositories.base.SoftDeleteRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends SoftDeleteRepository<Document, Integer> {
    
    

    @Query("SELECT d FROM Document d WHERE d.folder.idfolder = :folderId AND d.status = com.example.project.enums.EntityStatus.ACTIVE")
    List<Document> findActiveByFolderId(@Param("folderId") Integer folderId);
    
    

    @Query("SELECT d FROM Document d WHERE d.name LIKE %:name% AND d.status = com.example.project.enums.EntityStatus.ACTIVE")
    List<Document> findActiveByNameContaining(@Param("name") String name);
    
    

    @Query("SELECT d FROM Document d WHERE d.folder.idfolder = :folderId AND d.status = com.example.project.enums.EntityStatus.DELETED")
    List<Document> findDeletedByFolderId(@Param("folderId") Integer folderId);
    
    

    @Query("SELECT d FROM Document d WHERE d.folder.project = :project AND d.status = com.example.project.enums.EntityStatus.DELETED")
    @EntityGraph(attributePaths = {
        "folder",
        "folder.parentFolder",
        "versions"
    })
    List<Document> findDeletedByProjectWithParent(@Param("project") Project project);
}