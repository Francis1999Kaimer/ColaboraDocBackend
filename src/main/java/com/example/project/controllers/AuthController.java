package com.example.project.controllers;

import java.util.HashMap;
import java.util.Map;
import java.time.Duration; // Import Duration

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders; // Import HttpHeaders
import org.springframework.http.ResponseCookie; // Import ResponseCookie
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.DTO.RegisterRequest;
import com.example.project.config.JwtUtil;
import com.example.project.entities.User;
import com.example.project.repositories.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
// Remove HttpServletResponse import if not used elsewhere after changes
// import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
// Ensure your CORS allows credentials for SameSite=None to work
@CrossOrigin(origins = "https://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final JwtUtil jwtUtil;

    // Constructor injection is generally preferred
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Constructor injection for JwtUtil
    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya existe.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNames(request.getNames());
        user.setLasnames(request.getLastnames());
        user.setUserpassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Usuario registrado exitosamente.");
    }



    @PostMapping("/login")
    // Remove HttpServletResponse from parameters, we'll use ResponseEntity headers
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
            .map(user -> {
                if (passwordEncoder.matches(request.getPassword(), user.getUserpassword())) {
                    // ✅ Generar el token JWT
                    String token = jwtUtil.generateToken(user.getEmail(), user.getIduser());

                    // ✅ Crear la cookie httpOnly con el JWT using ResponseCookie
                    ResponseCookie cookie = ResponseCookie.from("token", token)
                        .httpOnly(true)       // Accessible only by the web server
                        .secure(true)         // Requires HTTPS
                        .path("/")            // Available for all paths
                        .maxAge(Duration.ofDays(1)) // Use Duration for maxAge (86400 seconds)
                        .sameSite("None")     // Crucial for cross-origin requests with credentials
                        .build();

                    // ✅ Devolver los datos del usuario sin el token en el cuerpo de la respuesta
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("email", user.getEmail());
                    responseMap.put("names", user.getNames());

                    // ✅ Add the cookie to the response headers
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(responseMap);
                } else {
                    // Use standard status code for invalid credentials
                    return ResponseEntity.status(401).body("Credenciales inválidas.");
                }
            })
            // Use standard status code for user not found / invalid credentials
            .orElse(ResponseEntity.status(401).body("Credenciales inválidas."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserData(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = null;

        // Busca la cookie 'token'
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Validate the token presence and validity
        if (token == null) {
             return ResponseEntity.status(401).body("No autorizado: Token no encontrado");
        }
        if (!jwtUtil.validateToken(token)) {
             return ResponseEntity.status(401).body("No autorizado: Token inválido o expirado");
        }


        String username = jwtUtil.getUsername(token);
        User user = userRepository.findByEmail(username).orElse(null);

        if (user == null) {
            // This shouldn't happen if the token is valid, but good practice
            return ResponseEntity.status(404).body("Usuario no encontrado para el token proporcionado");
        }

        // Devolver los datos del usuario
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("email", user.getEmail());
        responseMap.put("names", user.getNames());

        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/logout")
    // Remove HttpServletRequest and HttpServletResponse, use ResponseEntity
    public ResponseEntity<String> logout() {
        System.out.println("Received logout request."); // Add logging

        // ✅ Create an expired cookie with SameSite=None and Secure=true to clear it
        ResponseCookie cookie = ResponseCookie.from("token", "") // Empty value
            .httpOnly(true)
            .secure(true)         // MUST match the login cookie's secure attribute
            .path("/")            // MUST match the login cookie's path
            .maxAge(0)            // Expire immediately
            .sameSite("None")     // MUST match the login cookie's SameSite attribute
            .build();

        System.out.println("Logout cookie set to expire."); // Add logging

        // ✅ Add the expired cookie to the response headers and return OK
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout successful");
    }

    // Inner class for LoginRequest remains the same
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}