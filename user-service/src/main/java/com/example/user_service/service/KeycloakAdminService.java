package com.example.user_service.service;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KeycloakAdminService {

    @Value("${keycloak.server-url}")  private String serverUrl;
    @Value("${keycloak.realm}")       private String realm;
    @Value("${keycloak.client-id}")   private String clientId;
    @Value("${keycloak.client-secret}") private String clientSecret;

    private Keycloak getKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl).realm(realm)
                .clientId(clientId).clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    public String createUser(String email, String firstName,
                             String lastName, String password) {

        // 1. Vérifier si le user existe déjà dans Keycloak
        List<UserRepresentation> existing = getKeycloak()
                .realm(realm).users()
                .searchByEmail(email, true);

        if (!existing.isEmpty()) {
            // User existe déjà → retourner son ID directement
            String existingId = existing.get(0).getId();
            log.info("User already exists in Keycloak: {}", existingId);
            return existingId;
        }

        // 2. Créer le user
        UserRepresentation user = new UserRepresentation();
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);
        user.setCredentials(List.of(cred));

        Response response = getKeycloak().realm(realm).users().create(user);

        // 3. Vérifier le statut de la réponse
        if (response.getStatus() == 409) {
            // Double check — récupérer l'ID
            return getKeycloak().realm(realm).users()
                    .searchByEmail(email, true)
                    .get(0).getId();
        }

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: HTTP " 
                + response.getStatus());
        }

        // 4. Extraire le keycloakId depuis le header Location
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
