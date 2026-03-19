package com.example.user_service.kafka;

import com.example.user_service.model.User;
import java.time.Instant;

/**
 * C'est le "message" qu'on envoie dans Kafka.
 * Record Java = immuable, parfait pour les events.
 *
 * Ce message sera sérialisé en JSON automatiquement :
 * {
 *   "eventType": "CREATED",
 *   "userId": "550e8400-...",
 *   "keycloakId": "abc123",
 *   "email": "john@test.com",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 */
public record UserEvent(

    String eventType,   // "CREATED" | "UPDATED" | "DELETED"
    String userId,      // UUID dans notre PostgreSQL
    String keycloakId,  // ID dans Keycloak (sub du JWT)
    String email,
    String firstName,
    String lastName,
    Instant timestamp   // Quand l'event s'est produit

) {

    // ── Factory methods — façon propre de créer chaque type d'event ──

    /**
     * Appelé quand un user vient d'être créé
     */
    public static UserEvent created(User user) {
        return new UserEvent(
            "CREATED",
            user.getId().toString(),
            user.getKeycloakId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            Instant.now()
        );
    }

    /**
     * Appelé quand un user vient d'être modifié
     */
    public static UserEvent updated(User user) {
        return new UserEvent(
            "UPDATED",
            user.getId().toString(),
            user.getKeycloakId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            Instant.now()
        );
    }

    /**
     * Appelé quand un user vient d'être supprimé
     * On n'a plus accès à l'objet User complet → on passe juste les IDs
     */
    public static UserEvent deleted(String userId, String keycloakId) {
        return new UserEvent(
            "DELETED",
            userId,
            keycloakId,
            null,   // email non disponible après suppression
            null,
            null,
            Instant.now()
        );
    }
}
