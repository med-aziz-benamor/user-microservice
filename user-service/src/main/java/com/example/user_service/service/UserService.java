package com.example.userservice.service;

import com.example.userservice.dto.*;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import com.example.userservice.model.UserStatus;
import com.example.userservice.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository       userRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UserMapper           userMapper;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        // 1. Vérifier que l'email n'existe pas déjà
        if (userRepository.existsByEmail(request.email()))
            throw new EntityExistsException("Email already used: " + request.email());

        // 2. Créer dans Keycloak → récupérer le keycloakId
        String keycloakId = keycloakAdminService.createUser(
                request.email(), request.firstName(),
                request.lastName(), request.password());

        // 3. Sauvegarder dans PostgreSQL
        User user = User.builder()
                .keycloakId(keycloakId)
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        log.info("User created: id={}, email={}", user.getId(), user.getEmail());
        return userMapper.toResponse(user);
    }

    // @Cacheable → si le résultat est déjà dans Redis, on ne fait pas la requête BDD
    @Cacheable(value = "users", key = "#keycloakId")
    public UserResponse findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + keycloakId));
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAllActive(pageable)
                .map(userMapper::toResponse);
    }

    // @CachePut → met à jour le cache Redis après la modification
    @CachePut(value = "users", key = "#keycloakId")
    @Transactional
    public UserResponse update(String keycloakId, UpdateUserRequest request) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + keycloakId));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName()  != null) user.setLastName(request.lastName());

        return userMapper.toResponse(userRepository.save(user));
    }

    // @CacheEvict → supprime l'entrée du cache Redis
    @CacheEvict(value = "users", key = "#keycloakId")
    @Transactional
    public void delete(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + keycloakId));

        // Soft delete — on ne supprime pas vraiment de la BDD
        user.setDeletedAt(OffsetDateTime.now());
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        // Supprimer aussi dans Keycloak
        keycloakAdminService.deleteUser(keycloakId);
        log.info("User soft-deleted: keycloakId={}", keycloakId);
    }
}
