package com.example.project.controllers;

import com.example.project.DTO.AnnotationDTO;
import com.example.project.DTO.AnnotationWebSocketMessage;
import com.example.project.DTO.UserSummaryDTO;
import com.example.project.entities.User;
import com.example.project.repositories.UserRepository;
import com.example.project.services.AnnotationService;
import com.example.project.services.dtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Controller
public class AnnotationWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationWebSocketController.class);

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private dtoMapper dtoMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    
    private final ConcurrentMap<Integer, ConcurrentMap<String, UserSummaryDTO>> activeUsers = new ConcurrentHashMap<>();

    

    @SubscribeMapping("/topic/annotations/{versionId}")
    public List<AnnotationDTO> subscribeToAnnotations(@DestinationVariable Integer versionId, Principal principal) {
        try {
            
            User currentUser = getCurrentUser(principal);
            UserSummaryDTO userSummary = dtoMapper.toUserSummaryDTO(currentUser);

            
            activeUsers.computeIfAbsent(versionId, k -> new ConcurrentHashMap<>())
                      .put(principal.getName(), userSummary);

            
            AnnotationWebSocketMessage joinMessage = AnnotationWebSocketMessage.userJoined(versionId, userSummary);
            messagingTemplate.convertAndSend("/topic/annotations/" + versionId + "/events", joinMessage);

            
            return annotationService.getAnnotationsByVersion(versionId);

        } catch (Exception e) {
            logger.error("Error al suscribirse a anotaciones de versión {}: {}", versionId, e.getMessage());
            return List.of();
        }
    }

    

    @MessageMapping("/annotations/{versionId}/create")
    @SendTo("/topic/annotations/{versionId}")
    public AnnotationWebSocketMessage createAnnotation(@DestinationVariable Integer versionId, 
                                                      AnnotationDTO annotationData, 
                                                      Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            UserSummaryDTO userSummary = dtoMapper.toUserSummaryDTO(currentUser);            
            AnnotationDTO createdAnnotation = annotationService.createAnnotation(versionId, currentUser.getIduser(), annotationData);

            
            return AnnotationWebSocketMessage.createAnnotation(versionId, createdAnnotation, userSummary);

        } catch (Exception e) {
            logger.error("Error al crear anotación: {}", e.getMessage());
            
            AnnotationWebSocketMessage errorMessage = new AnnotationWebSocketMessage();
            errorMessage.setAction(AnnotationWebSocketMessage.Action.CREATE);
            return errorMessage;
        }
    }

    

    @MessageMapping("/annotations/{versionId}/update")
    @SendTo("/topic/annotations/{versionId}")
    public AnnotationWebSocketMessage updateAnnotation(@DestinationVariable Integer versionId,
                                                      AnnotationDTO annotationData,
                                                      Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            UserSummaryDTO userSummary = dtoMapper.toUserSummaryDTO(currentUser);            
            AnnotationDTO updatedAnnotation = annotationService.updateAnnotation(annotationData.getIdannotation(), currentUser.getIduser(), annotationData);

            return AnnotationWebSocketMessage.updateAnnotation(versionId, updatedAnnotation, userSummary);

        } catch (Exception e) {
            logger.error("Error al actualizar anotación: {}", e.getMessage());
            return null;
        }
    }

    

    @MessageMapping("/annotations/{versionId}/delete")
    @SendTo("/topic/annotations/{versionId}")
    public AnnotationWebSocketMessage deleteAnnotation(@DestinationVariable Integer versionId,
                                                      AnnotationDTO annotationData,
                                                      Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            UserSummaryDTO userSummary = dtoMapper.toUserSummaryDTO(currentUser);            
            annotationService.deleteAnnotation(annotationData.getIdannotation(), currentUser.getIduser());

            return AnnotationWebSocketMessage.deleteAnnotation(versionId, annotationData, userSummary);

        } catch (Exception e) {
            logger.error("Error al eliminar anotación: {}", e.getMessage());
            return null;
        }
    }

    

    @MessageMapping("/annotations/{versionId}/cursor")
    @SendTo("/topic/annotations/{versionId}/cursors")
    public AnnotationWebSocketMessage cursorMovement(@DestinationVariable Integer versionId,
                                                    AnnotationWebSocketMessage cursorData,
                                                    Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            UserSummaryDTO userSummary = dtoMapper.toUserSummaryDTO(currentUser);

            return AnnotationWebSocketMessage.cursorMove(
                versionId, 
                cursorData.getPageNumber(), 
                cursorData.getCoordinates(), 
                userSummary
            );

        } catch (Exception e) {
            logger.error("Error en movimiento de cursor: {}", e.getMessage());
            return null;
        }
    }

    

    @MessageMapping("/annotations/{versionId}/leave")
    @SendTo("/topic/annotations/{versionId}/events")
    public AnnotationWebSocketMessage userLeaving(@DestinationVariable Integer versionId, Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            UserSummaryDTO userSummary = dtoMapper.toUserSummaryDTO(currentUser);

            
            ConcurrentMap<String, UserSummaryDTO> versionUsers = activeUsers.get(versionId);
            if (versionUsers != null) {
                versionUsers.remove(principal.getName());
                if (versionUsers.isEmpty()) {
                    activeUsers.remove(versionId);
                }
            }

            return AnnotationWebSocketMessage.userLeft(versionId, userSummary);

        } catch (Exception e) {
            logger.error("Error al procesar salida de usuario: {}", e.getMessage());
            return null;
        }
    }

    

    @SubscribeMapping("/topic/annotations/{versionId}/users")
    public List<UserSummaryDTO> getActiveUsers(@DestinationVariable Integer versionId) {
        ConcurrentMap<String, UserSummaryDTO> versionUsers = activeUsers.get(versionId);
        if (versionUsers != null) {
            return List.copyOf(versionUsers.values());
        }
        return List.of();
    }    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        final String email;
        if (principal instanceof Authentication) {
            email = ((Authentication) principal).getName();
        } else {
            email = principal.getName();
        }
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
}
