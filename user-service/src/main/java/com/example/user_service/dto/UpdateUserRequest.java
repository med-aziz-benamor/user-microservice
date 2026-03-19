
package com.example.user_service.dto;

import jakarta.validation.constraints.Size;

/**

 * Tous les champs sont optionnels — on ne modifie que ce qui est fourni

 */

public record UpdateUserRequest(

    @Size(min = 2, message = "First name must be at least 2 characters")

    String firstName,

    @Size(min = 2, message = "Last name must be at least 2 characters")

    String lastName

) {}

