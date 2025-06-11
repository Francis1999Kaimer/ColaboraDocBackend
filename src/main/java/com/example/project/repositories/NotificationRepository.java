package com.example.project.repositories;

import com.example.project.entities.Notification;
import com.example.project.entities.User;
import com.example.project.entities.Project;
import com.example.project.repositories.base.SoftDeleteRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends SoftDeleteRepository<Notification, Long> {
    
    

    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.status = com.example.project.enums.EntityStatus.ACTIVE ORDER BY n.createdAt DESC")
    List<Notification> findActiveByRecipient(@Param("recipient") User recipient);
    
    

    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false AND n.status = com.example.project.enums.EntityStatus.ACTIVE ORDER BY n.createdAt DESC")
    List<Notification> findUnreadActiveByRecipient(@Param("recipient") User recipient);
    
    

    @Query("SELECT n FROM Notification n WHERE n.project = :project AND n.status = com.example.project.enums.EntityStatus.ACTIVE ORDER BY n.createdAt DESC")
    List<Notification> findActiveByProject(@Param("project") Project project);
    
    

    @Query("SELECT n FROM Notification n WHERE n.entityType = :entityType AND n.entityId = :entityId AND n.status = com.example.project.enums.EntityStatus.ACTIVE ORDER BY n.createdAt DESC")
    List<Notification> findActiveByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Integer entityId);
    
    

    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.status = com.example.project.enums.EntityStatus.DELETED ORDER BY n.deletedAt DESC")
    List<Notification> findDeletedByRecipient(@Param("recipient") User recipient);
    
    

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false AND n.status = com.example.project.enums.EntityStatus.ACTIVE")
    Long countUnreadActiveByRecipient(@Param("recipient") User recipient);
}
