package com.example.project.controllers;

import com.example.project.DTO.UserSummaryDTO;
import com.example.project.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CAN_INVITE_USERS')") 
    public ResponseEntity<List<UserSummaryDTO>> getAllUserSummaries() {
        logger.info("Solicitud para obtener el resumen de todos los usuarios.");
        List<UserSummaryDTO> users = userService.getAllUserSummaries();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/{userId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSummaryDTO> getUserSummary(@PathVariable Integer userId) {
        logger.info("Solicitud para obtener el resumen del usuario con ID: {}", userId);
        UserSummaryDTO user = userService.getUserSummaryById(userId);
        return ResponseEntity.ok(user);
    }

}