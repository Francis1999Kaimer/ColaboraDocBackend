package com.example.project.repositories;

import com.example.project.entities.Version;
import com.example.project.entities.Project;
import com.example.project.repositories.base.SoftDeleteRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends SoftDeleteRepository<Version, Integer> {
    
    

    @Query("SELECT v FROM Version v WHERE v.document.iddocument = :documentId AND v.status = 'ACTIVE' ORDER BY v.versionNumber DESC")
    List<Version> findActiveByDocumentId(@Param("documentId") Integer documentId);
    
    

    @Query("SELECT v FROM Version v WHERE v.document.iddocument = :documentId AND v.status = 'ACTIVE' ORDER BY v.versionNumber DESC")
    List<Version> findLatestActiveByDocumentId(@Param("documentId") Integer documentId);
      

    @Query("SELECT v FROM Version v WHERE v.document.iddocument = :documentId AND v.status = 'DELETED' ORDER BY v.versionNumber DESC")
    List<Version> findDeletedByDocumentId(@Param("documentId") Integer documentId);
    
    

    @Query("SELECT v FROM Version v WHERE v.document.folder.project = :project AND v.status = 'DELETED' ORDER BY v.versionNumber DESC")
    List<Version> findDeletedByProject(@Param("project") Project project);
}