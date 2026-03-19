
package com.example.userservice.exception;

import jakarta.persistence.EntityExistsException;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**

 * Intercepte toutes les exceptions et retourne du JSON propre

 * au lieu d'une page d'erreur HTML

 */

@RestControllerAdvice

public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)

    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {

        return ResponseEntity.status(403)

                .body(new ErrorResponse(403, "Forbidden", "Access denied"));

    }

    @ExceptionHandler(AuthenticationException.class)

    public ResponseEntity<ErrorResponse> handleUnauthorized(AuthenticationException ex) {

        return ResponseEntity.status(401)

                .body(new ErrorResponse(401, "Unauthorized", "Invalid or missing token"));

    }

    @ExceptionHandler(EntityNotFoundException.class)

    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {

        return ResponseEntity.status(404)

                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));

    }

    @ExceptionHandler(EntityExistsException.class)

    public ResponseEntity<ErrorResponse> handleConflict(EntityExistsException ex) {

        return ResponseEntity.status(409)

                .body(new ErrorResponse(409, "Conflict", ex.getMessage()));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream()

                .map(e -> e.getField() + ": " + e.getDefaultMessage())

                .collect(Collectors.joining(", "));

        return ResponseEntity.status(400)

                .body(new ErrorResponse(400, "Bad Request", message));

    }

    // Record = classe simple avec champs immuables (Java 16+)

    public record ErrorResponse(int status, String error, String message) {}

}

