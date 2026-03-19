package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/users — public, pas besoin de token
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(201).body(userService.create(req));
    }

    // GET /api/users/me — retourne le profil de l'utilisateur connecté
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication auth) {
        // Le JWT est dans auth.getPrincipal()
        // getSubject() = le "sub" claim = keycloakId
        String keycloakId = ((Jwt) auth.getPrincipal()).getSubject();
        return ResponseEntity.ok(userService.findByKeycloakId(keycloakId));
    }

    // GET /api/users — admin seulement
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.findAll(PageRequest.of(page, size)));
    }

    // GET /api/users/{keycloakId}
    @GetMapping("/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(
            @PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.findByKeycloakId(keycloakId));
    }

    // PATCH /api/users/{keycloakId}
    @PatchMapping("/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable String keycloakId,
            @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.update(keycloakId, req));
    }

    // DELETE /api/users/{keycloakId}
    @DeleteMapping("/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String keycloakId) {
        userService.delete(keycloakId);
        return ResponseEntity.noContent().build();
    }
}
