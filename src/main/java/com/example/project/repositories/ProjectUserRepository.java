package com.example.project.repositories;

import com.example.project.entities.ProjectUser;
import com.example.project.entities.User;
import com.example.project.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProjectUserRepository extends JpaRepository<ProjectUser, Integer> {
    List<ProjectUser> findByUser(User user);
    List<ProjectUser> findByProject(Project project);
    Optional<ProjectUser> findByUserAndProject(User user, Project project);      List<ProjectUser> findByUserAndStatusInvitacion(User user, ProjectUser.InvitationStatus statusInvitacion);
    Optional<ProjectUser> findByUserAndProjectAndStatusInvitacionIn(User user, Project project, List<ProjectUser.InvitationStatus> statusesInvitacion);
    
    
    @Query("SELECT pu FROM ProjectUser pu WHERE pu.user = :user AND pu.statusInvitacion = :statusInvitacion AND pu.status = 'ACTIVE'")
    List<ProjectUser> findActiveByUserAndStatusInvitacion(@Param("user") User user, @Param("statusInvitacion") ProjectUser.InvitationStatus statusInvitacion);
    
    

    @Query("SELECT pu FROM ProjectUser pu WHERE pu.project = :project AND pu.statusInvitacion = :statusInvitacion AND pu.status = 'ACTIVE'")
    List<ProjectUser> findActiveByProjectAndStatusInvitacion(@Param("project") Project project, @Param("statusInvitacion") ProjectUser.InvitationStatus statusInvitacion);
      

    @Query("SELECT pu FROM ProjectUser pu WHERE pu.user.email = :userEmail AND pu.project.idproject = :projectId AND pu.statusInvitacion = :statusInvitacion")
    Optional<ProjectUser> findByUserEmailAndProjectIdAndStatusInvitacion(@Param("userEmail") String userEmail, @Param("projectId") Integer projectId, @Param("statusInvitacion") ProjectUser.InvitationStatus statusInvitacion);
    
    

    @Query("SELECT pu FROM ProjectUser pu WHERE pu.project = :project AND pu.statusInvitacion = 'ACCEPTED' AND pu.status = 'DELETED'")
    List<ProjectUser> findDeletedUsersByProject(@Param("project") Project project);
    
    

    @Query("SELECT pu FROM ProjectUser pu WHERE pu.user.iduser = :userId AND pu.project = :project AND pu.statusInvitacion = 'ACCEPTED' AND pu.status = 'DELETED'")
    Optional<ProjectUser> findDeletedUserByUserIdAndProject(@Param("userId") Integer userId, @Param("project") Project project);
}