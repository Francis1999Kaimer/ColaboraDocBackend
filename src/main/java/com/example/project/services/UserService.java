package com.example.project.services;

import com.example.project.DTO.UserSummaryDTO;
import com.example.project.entities.User;
import com.example.project.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final dtoMapper dtoMapper;

    public UserService(UserRepository userRepository, dtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getAllUserSummaries() {
        return userRepository.findAll().stream()
                .map(dtoMapper::toUserSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserSummaryDTO getUserSummaryById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + userId));
        return dtoMapper.toUserSummaryDTO(user);
    }

}