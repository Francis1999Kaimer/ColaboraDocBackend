package com.example.project.services;

import com.example.project.DTO.NotificationDTO;
import com.example.project.entities.Notification;
import com.example.project.entities.User;
import com.example.project.entities.Project;
import com.example.project.repositories.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private dtoMapper dtoMapper;

    @Autowired
    private SoftDeleteService softDeleteService;

    

    @Transactional
    public NotificationDTO createNotification(User recipient, String message, Project project, 
                                             String entityType, Integer entityId, User triggeredBy) {
        Notification notification = new Notification(recipient, message, project, entityType, entityId, triggeredBy);
        Notification savedNotification = notificationRepository.save(notification);
        return dtoMapper.toNotificationDTO(savedNotification);
    }

    

    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findByIdAndActive(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con ID: " + notificationId));
        return dtoMapper.toNotificationDTO(notification);
    }

    

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForUser(User user) {
        return notificationRepository.findActiveByRecipient(user)
                .stream()
                .map(dtoMapper::toNotificationDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsForUser(User user) {
        return notificationRepository.findUnreadActiveByRecipient(user)
                .stream()
                .map(dtoMapper::toNotificationDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional(readOnly = true)
    public Long getUnreadNotificationCount(User user) {
        return notificationRepository.countUnreadActiveByRecipient(user);
    }

    

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByProject(Project project) {
        return notificationRepository.findActiveByProject(project)
                .stream()
                .map(dtoMapper::toNotificationDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByEntity(String entityType, Integer entityId) {
        return notificationRepository.findActiveByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(dtoMapper::toNotificationDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional
    public NotificationDTO markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findByIdAndActive(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con ID: " + notificationId));
        
        
        if (!notification.getRecipient().getIduser().equals(user.getIduser())) {
            throw new IllegalArgumentException("No tienes permisos para modificar esta notificación");
        }
        
        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        return dtoMapper.toNotificationDTO(savedNotification);
    }

    

    @Transactional
    public NotificationDTO markAsUnread(Long notificationId, User user) {
        Notification notification = notificationRepository.findByIdAndActive(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con ID: " + notificationId));
        
        
        if (!notification.getRecipient().getIduser().equals(user.getIduser())) {
            throw new IllegalArgumentException("No tienes permisos para modificar esta notificación");
        }
        
        notification.setRead(false);
        Notification savedNotification = notificationRepository.save(notification);
        return dtoMapper.toNotificationDTO(savedNotification);
    }

    

    @Transactional
    public void markAllAsReadForUser(User user) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadActiveByRecipient(user);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    

    @Transactional
    public void deleteNotification(Long notificationId, User user, String deletedBy) {
        Notification notification = notificationRepository.findByIdAndActive(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con ID: " + notificationId));
        
        
        if (!notification.getRecipient().getIduser().equals(user.getIduser())) {
            throw new IllegalArgumentException("No tienes permisos para eliminar esta notificación");
        }
        
        softDeleteService.softDelete(notification, deletedBy);
        notificationRepository.save(notification);
    }

    

    @Transactional
    public void restoreNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con ID: " + notificationId));
        
        if (!notification.isDeleted()) {
            throw new IllegalStateException("La notificación con ID " + notificationId + " no está eliminada");
        }
        
        notification.restore();
        notificationRepository.save(notification);
    }

    

    @Transactional(readOnly = true)
    public List<NotificationDTO> getDeletedNotificationsForUser(User user) {
        return notificationRepository.findDeletedByRecipient(user)
                .stream()
                .map(dtoMapper::toNotificationDTO)
                .collect(Collectors.toList());
    }

    

    @Transactional
    public void cleanupNotificationsForEntity(String entityType, Integer entityId, String deletedBy) {
        List<Notification> notifications = notificationRepository.findActiveByEntityTypeAndEntityId(entityType, entityId);
        for (Notification notification : notifications) {
            softDeleteService.softDelete(notification, deletedBy);
        }
        notificationRepository.saveAll(notifications);
    }

    

    @Transactional
    public void cleanupNotificationsForProject(Project project, String deletedBy) {
        List<Notification> notifications = notificationRepository.findActiveByProject(project);
        for (Notification notification : notifications) {
            softDeleteService.softDelete(notification, deletedBy);
        }
        notificationRepository.saveAll(notifications);
    }
}
