package com.example.project.controllers;

import com.example.project.DTO.LoginRequestDTO;
import com.example.project.DTO.LoginResponseDTO;
import com.example.project.DTO.RegisterRequest;
import com.example.project.DTO.UserMeResponseDTO;
import com.example.project.config.JwtUtil;
import com.example.project.entities.User;
import com.example.project.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtUtil jwtUtil;


    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("Usuario registrado exitosamente.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        User user = authService.authenticateUser(request);
        String token = jwtUtil.generateToken(user.getEmail(), user.getIduser());
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true).secure(true).path("/").maxAge(Duration.ofDays(1)).sameSite("None").build();
        LoginResponseDTO responseBody = new LoginResponseDTO(user.getIduser(), user.getEmail(), user.getNames(), user.getLastnames());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(responseBody);
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDTO> getUserData(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) { if ("token".equals(cookie.getName())) { token = cookie.getValue(); break; } }
        }
        if (token == null) {
            logger.warn("Intento de acceso a /me sin token.");
            return ResponseEntity.status(401).body(null);
        }
        if (!jwtUtil.validateToken(token)) {
            logger.warn("Intento de acceso a /me con token inválido o expirado.");
            return ResponseEntity.status(401).body(null);
        }
        String username = jwtUtil.getUsername(token);
        User user = authService.findByEmail(username);
        UserMeResponseDTO responseBody = new UserMeResponseDTO(user.getIduser(), user.getEmail(), user.getNames(), user.getLastnames());
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        logger.info("Solicitud de logout recibida.");
        ResponseCookie cookie = ResponseCookie.from("token", "").httpOnly(true).secure(true).path("/").maxAge(0).sameSite("None").build();
        logger.info("Cookie de logout configurada para expirar.");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("Logout exitoso");
    }

 
}