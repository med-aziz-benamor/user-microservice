
package com.example.user_service.dto;

import java.time.OffsetDateTime;

import java.util.UUID;

/**

 * On ne renvoie jamais le mot de passe ni deleted_at au client

 */

public record UserResponse(

    UUID id,

    String keycloakId,

    String email,

    String firstName,

    String lastName,

    String status,

    OffsetDateTime createdAt

) {}

