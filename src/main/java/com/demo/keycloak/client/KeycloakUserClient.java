package com.demo.keycloak.client;

import com.demo.keycloak.model.KeycloakUser;
import com.demo.keycloak.model.UserRepresentation;

import java.util.Optional;

public interface KeycloakUserClient {
    Optional<KeycloakUser> getUserByUsername(String username);

    void createAccount(KeycloakUser keycloakUser);

    <T extends UserRepresentation> void updateUser(String id, T userRepresentation);
}
