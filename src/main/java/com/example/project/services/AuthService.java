package com.example.project.services;

import com.example.project.DTO.LoginRequestDTO;
import com.example.project.DTO.RegisterRequest;
import com.example.project.entities.User;
import com.example.project.exception.UserAlreadyExistsException;
import com.example.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder /*, DtoMapper dtoMapper */) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
       
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("El email '" + request.getEmail() + "' ya está registrado.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNames(request.getNames());
        user.setLastnames(request.getLastnames());
        user.setUserpassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User authenticateUser(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas."));

        if (!passwordEncoder.matches(request.getPassword(), user.getUserpassword())) {
            throw new BadCredentialsException("Credenciales inválidas.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }


}