package com.example.project.repositories;

import com.example.project.entities.Folder;
import com.example.project.entities.Project;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Integer> {

    @EntityGraph(attributePaths = {
        "documents",          
        "childFolders",           
        "childFolders.documents",  
        "childFolders.childFolders" 
    })
    List<Folder> findByProjectAndParentFolderIsNull(Project project);
}