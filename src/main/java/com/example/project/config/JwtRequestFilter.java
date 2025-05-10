package com.example.project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Importar UserDetails

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService; // Inyectar CustomUserDetailsService


    public JwtRequestFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String jwt = null;
        String username = null; // Este será el email

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.getUsername(jwt);
            } catch (Exception e) {
                logger.warn("Error al extraer username del token en Header: " + e.getMessage());
            }
        }

        if (jwt == null && request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
            if (jwt != null) {
                 try {
                    username = jwtUtil.getUsername(jwt);
                } catch (Exception e) {
                    logger.warn("Error al extraer username del token en Cookie: " + e.getMessage());
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Cargar UserDetails usando el servicio
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validar el token (confirmar que el token es para el UserDetails cargado y no ha expirado, etc.)
            // El método validateToken del JwtUtil ya verifica la firma y la expiración.
            // Podrías añadir una comprobación de que userDetails.getUsername() coincide con el username del token si es necesario,
            // pero si el token se valida correctamente, el username extraído del token es el que se usa para cargar UserDetails.
            if (jwtUtil.validateToken(jwt)) { // Asegúrate que validateToken(jwt, userDetails) no sea necesario,
                                              // validateToken(jwt) debería ser suficiente si solo verifica el token en sí.
                
                // Crear el objeto de autenticación usando el UserDetails completo
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, // <--- El principal ahora es UserDetails
                                null,        // Credenciales (contraseña) no son necesarias aquí
                                userDetails.getAuthorities() // Authorities/roles del UserDetails
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Usuario autenticado via JWT: " + username + " con authorities: " + userDetails.getAuthorities());
            } else {
                 logger.warn("Token JWT inválido recibido para usuario: " + username);
            }
        } else if (username == null && (authorizationHeader != null || (request.getCookies() != null && request.getCookies().length > 0))) {
             logger.debug("No se pudo extraer username del token o no se encontró token.");
        }

        chain.doFilter(request, response);
    }
}