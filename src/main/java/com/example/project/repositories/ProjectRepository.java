package com.example.project.repositories;

import com.example.project.entities.Project;
import com.example.project.repositories.base.SoftDeleteRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends SoftDeleteRepository<Project, Integer> {
    
    

    @Query("SELECT p FROM Project p WHERE p.name LIKE %:name% AND p.status = 'ACTIVE'")
    List<Project> findActiveByNameContaining(@Param("name") String name);
}