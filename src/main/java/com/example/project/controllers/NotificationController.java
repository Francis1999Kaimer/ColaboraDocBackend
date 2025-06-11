package com.example.project.controllers;

import com.example.project.DTO.NotificationDTO;
import com.example.project.entities.User;
import com.example.project.repositories.UserRepository;
import com.example.project.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(user);
        return ResponseEntity.ok(notifications);
    }

    

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        List<NotificationDTO> unreadNotifications = notificationService.getUnreadNotificationsForUser(user);
        return ResponseEntity.ok(unreadNotifications);
    }

    

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        Long count = notificationService.getUnreadNotificationCount(user);
        return ResponseEntity.ok(count);
    }

    

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long notificationId, 
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        NotificationDTO notification = notificationService.markAsRead(notificationId, user);
        return ResponseEntity.ok(notification);
    }

    

    @PutMapping("/{notificationId}/unread")
    public ResponseEntity<NotificationDTO> markAsUnread(@PathVariable Long notificationId, 
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        NotificationDTO notification = notificationService.markAsUnread(notificationId, user);
        return ResponseEntity.ok(notification);
    }

    

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        notificationService.markAllAsReadForUser(user);
        return ResponseEntity.ok().build();
    }

    

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId, 
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromDetails(userDetails);
        notificationService.deleteNotification(notificationId, user, user.getEmail());
        return ResponseEntity.ok().build();
    }

    

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long notificationId) {
        NotificationDTO notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(notification);
    }



    

    private User getUserFromDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userDetails.getUsername()));
    }
}
