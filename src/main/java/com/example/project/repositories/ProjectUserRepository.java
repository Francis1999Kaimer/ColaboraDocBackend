package com.example.project.repositories;

import com.example.project.entities.ProjectUser;
import com.example.project.entities.User;
import com.example.project.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectUserRepository extends JpaRepository<ProjectUser, Integer> {
    List<ProjectUser> findByUser(User user);
    List<ProjectUser> findByProject(Project project);
    Optional<ProjectUser> findByUserAndProject(User user, Project project);
    List<ProjectUser> findByUserAndStatus(User user, ProjectUser.InvitationStatus status);
    Optional<ProjectUser> findByUserAndProjectAndStatusIn(User user, Project project, List<ProjectUser.InvitationStatus> statuses);
}