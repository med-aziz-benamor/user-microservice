
package com.example.user_service.service;

import lombok.extern.slf4j.Slf4j;

import org.keycloak.OAuth2Constants;

import org.keycloak.admin.client.Keycloak;

import org.keycloak.admin.client.KeycloakBuilder;

import org.keycloak.representations.idm.CredentialRepresentation;

import org.keycloak.representations.idm.RoleRepresentation;

import org.keycloak.representations.idm.UserRepresentation;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;

import java.util.List;

/**

 * Communique avec l'API Admin de Keycloak

 * Permet de créer/supprimer des users directement dans Keycloak

 */

@Service

@Slf4j

public class KeycloakAdminService {

    @Value("${keycloak.server-url}")

    private String serverUrl;

    @Value("${keycloak.realm}")

    private String realm;

    @Value("${keycloak.client-id}")

    private String clientId;

    @Value("${keycloak.client-secret}")

    private String clientSecret;

    // Crée une connexion admin Keycloak avec les credentials du service

    private Keycloak getKeycloak() {

        return KeycloakBuilder.builder()

                .serverUrl(serverUrl)

                .realm(realm)

                .clientId(clientId)

                .clientSecret(clientSecret)

                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)

                .build();

    }

    /**

     * Crée un user dans Keycloak et retourne son ID (keycloakId)

     */

    public String createUser(String email, String firstName,

                             String lastName, String password) {

        UserRepresentation user = new UserRepresentation();

        user.setEmail(email);

        user.setUsername(email);

        user.setFirstName(firstName);

        user.setLastName(lastName);

        user.setEnabled(true);

        user.setEmailVerified(true);

        // Définir le mot de passe

        CredentialRepresentation cred = new CredentialRepresentation();

        cred.setType(CredentialRepresentation.PASSWORD);

        cred.setValue(password);

        cred.setTemporary(false);

        user.setCredentials(List.of(cred));

        Response response = getKeycloak().realm(realm).users().create(user);

        // Keycloak retourne l'ID dans le header "Location" : .../users/{id}

        String location = response.getHeaderString("Location");

        String keycloakId = location.substring(location.lastIndexOf("/") + 1);

        log.info("User created in Keycloak: {}", keycloakId);

        return keycloakId;

    }

    public void deleteUser(String keycloakId) {

        getKeycloak().realm(realm).users().get(keycloakId).remove();

        log.info("User deleted from Keycloak: {}", keycloakId);

    }

    public void assignRole(String keycloakId, String roleName) {

        RoleRepresentation role = getKeycloak()

                .realm(realm).roles().get(roleName).toRepresentation();

        getKeycloak().realm(realm).users().get(keycloakId)

                .roles().realmLevel().add(List.of(role));

    }

}

