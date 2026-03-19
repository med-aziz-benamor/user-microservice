package com.example.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité JPA = représentation Java de la table "users" en BDD
 * Lombok génère automatiquement : getters, setters, constructeurs, builder
 */
@Entity
@Table(name = "users")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // keycloak_id = le "sub" claim du JWT (identifiant unique Keycloak)
    @Column(name = "keycloak_id", nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    // EnumType.STRING = stocke "ACTIVE" en BDD, pas "2"
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    // updatable = false → ce champ ne change jamais après la création
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // @PrePersist → appelé automatiquement AVANT chaque INSERT en BDD
    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    // @PreUpdate → appelé automatiquement AVANT chaque UPDATE en BDD
    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
