package com.example.project.repositories;

import com.example.project.entities.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Integer> {
      

    @Query("SELECT a FROM Annotation a WHERE a.version.idversion = :versionId AND a.isActive = true ORDER BY a.createdAt ASC")
    List<Annotation> findByVersionId(@Param("versionId") Integer versionId);
    
    

    @Query("SELECT a FROM Annotation a WHERE a.version.idversion = :versionId AND a.createdBy.iduser = :userId AND a.isActive = true ORDER BY a.createdAt ASC")
    List<Annotation> findByVersionIdAndUserId(@Param("versionId") Integer versionId, @Param("userId") Integer userId);
    
    

    @Query("SELECT COUNT(a) FROM Annotation a WHERE a.version.idversion = :versionId AND a.isActive = true")
    Long countByVersionId(@Param("versionId") Integer versionId);      

    @Query("SELECT a FROM Annotation a WHERE a.version.idversion = :versionId AND a.annotationType = :type AND a.isActive = true ORDER BY a.createdAt ASC")
    List<Annotation> findByVersionIdAndType(@Param("versionId") Integer versionId, @Param("type") com.example.project.entities.AnnotationType type);
}
