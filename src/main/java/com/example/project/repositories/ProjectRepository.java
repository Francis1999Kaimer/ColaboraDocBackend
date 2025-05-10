package com.example.project.repositories;

import com.example.project.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    // Métodos personalizados si es necesario
}