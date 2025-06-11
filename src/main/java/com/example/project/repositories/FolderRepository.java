package com.example.project.repositories;

import com.example.project.entities.Folder;
import com.example.project.entities.Project;
import com.example.project.repositories.base.SoftDeleteRepository;
import com.example.project.enums.EntityStatus;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends SoftDeleteRepository<Folder, Integer> {

    

    @EntityGraph(attributePaths = {
        "documents",          
        "childFolders",           
        "childFolders.documents",  
        "childFolders.childFolders" 
    })    List<Folder> findByProjectAndParentFolderIsNull(Project project);

    

    
    @Query("SELECT f FROM Folder f WHERE f.project = :project AND f.parentFolder IS NULL AND f.status = :status")
    @EntityGraph(attributePaths = {
        "documents",          
        "childFolders",           
        "childFolders.documents",  
        "childFolders.childFolders" 
    })    List<Folder> findByProjectAndParentFolderIsNullAndStatus(@Param("project") Project project, @Param("status") EntityStatus status);   
     
    

    @Query("SELECT f FROM Folder f WHERE f.project = :project AND f.parentFolder IS NULL AND f.status = 'ACTIVE' AND f.folderType = 'REGULAR'")
    @EntityGraph(attributePaths = {
        "documents",          
        "childFolders",           
        "childFolders.documents",  
        "childFolders.childFolders" 
    })    List<Folder> findActiveByProjectAndParentFolderIsNull(@Param("project") Project project);

    

    @Query("SELECT f FROM Folder f WHERE f.project = :project AND f.parentFolder IS NULL AND f.status = 'DELETE' AND f.folderType = 'REGULAR'")
    @EntityGraph(attributePaths = {
        "documents",          
        "childFolders",           
        "childFolders.documents",  
        "childFolders.childFolders" 
    })    List<Folder> findDeletedByProjectAndParentFolderIsNull(@Param("project") Project project);

    

    @Query("SELECT f FROM Folder f WHERE f.parentFolder = :parentFolder AND f.status = 'ACTIVE'")    List<Folder> findActiveByParentFolder(@Param("parentFolder") Folder parentFolder);

    

    @Query("SELECT f FROM Folder f WHERE f.parentFolder = :parentFolder AND f.status = 'DELETED'")    List<Folder> findDeletedByParentFolder(@Param("parentFolder") Folder parentFolder);

    

    @Query("SELECT f FROM Folder f WHERE f.project = :project AND f.status = 'DELETED'")
    List<Folder> findDeletedByProject(@Param("project") Project project);

    

    @Query("SELECT f FROM Folder f WHERE f.project = :project AND f.status = 'DELETED'")
    @EntityGraph(attributePaths = {
        "documents",          
        "childFolders",           
        "childFolders.documents",  
        "childFolders.childFolders",
        "parentFolder"
    })
    List<Folder> findDeletedByProjectWithHierarchy(@Param("project") Project project);
}