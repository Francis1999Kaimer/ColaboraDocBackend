
package com.example.project.config;


import com.example.project.entities.User;
import com.example.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // Para las authorities si no tienes roles definidos

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Aquí 'username' es el email que usamos como identificador
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Creamos un UserDetails de Spring Security.
        // El primer argumento es el username (email), el segundo es la contraseña (ya validada por JWT),
        // y el tercero son las authorities (roles). Si no tienes un sistema de roles complejo aún,
        // puedes pasar una lista vacía.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getUserpassword(), // Aunque el JWT ya autenticó, es buena práctica incluirlo.
                                       // Spring Security no lo usará para re-autenticar en este flujo JWT.
                new ArrayList<>() // Lista de GrantedAuthority. Añade roles aquí si los tienes.
        );
    }
}