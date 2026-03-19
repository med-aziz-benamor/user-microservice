package com.example.userservice.service;

import com.example.userservice.dto.*;
import com.example.userservice.kafka.UserEventProducer;
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
    private final UserEventProducer    eventProducer;   // ← AJOUT M5

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new EntityExistsException("Email already used: " + request.email());

        String keycloakId = keycloakAdminService.createUser(
                request.email(), request.firstName(),
                request.lastName(), request.password());

        User user = User.builder()
                .keycloakId(keycloakId)
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        log.info("User created: id={}, email={}", user.getId(), user.getEmail());

        // ── M5 : Publier l'event Kafka APRÈS le save ──
        // On publie après le commit BDD pour éviter d'envoyer un event
        // si la transaction rollback
        eventProducer.publishUserCreated(user);

        return userMapper.toResponse(user);
    }

    @Cacheable(value = "users", key = "#keycloakId")
    public UserResponse findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .map(userMapper::toResponse)
                .orElseThrow(() ->
                    new EntityNotFoundException("User not found: " + keycloakId));
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAllActive(pageable)
                .map(userMapper::toResponse);
    }

    @CachePut(value = "users", key = "#keycloakId")
    @Transactional
    public UserResponse update(String keycloakId, UpdateUserRequest request) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() ->
                    new EntityNotFoundException("User not found: " + keycloakId));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName()  != null) user.setLastName(request.lastName());

        User saved = userRepository.save(user);

        // ── M5 : Publier l'event de mise à jour ──
        eventProducer.publishUserUpdated(saved);

        return userMapper.toResponse(saved);
    }

    @CacheEvict(value = "users", key = "#keycloakId")
    @Transactional
    public void delete(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() ->
                    new EntityNotFoundException("User not found: " + keycloakId));

        String userId = user.getId().toString(); // Sauvegarder avant soft delete

        user.setDeletedAt(OffsetDateTime.now());
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        keycloakAdminService.deleteUser(keycloakId);

        // ── M5 : Publier l'event de suppression ──
        eventProducer.publishUserDeleted(userId, keycloakId);

        log.info("User soft-deleted: keycloakId={}", keycloakId);
    }
}
