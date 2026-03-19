
package com.example.userservice.dto;

import jakarta.validation.constraints.*;

/**

 * Record Java = classe immuable avec constructeur, getters, equals, hashCode

 * Les annotations @NotBlank, @Email etc. déclenchent la validation automatique

 */

public record CreateUserRequest(

    @NotBlank(message = "Email is required")

    @Email(message = "Email must be valid")

    String email,

    @NotBlank(message = "First name is required")

    @Size(min = 2, message = "First name must be at least 2 characters")

    String firstName,

    @NotBlank(message = "Last name is required")

    @Size(min = 2, message = "Last name must be at least 2 characters")

    String lastName,

    @NotBlank(message = "Password is required")

    @Size(min = 8, message = "Password must be at least 8 characters")

    String password

) {}

