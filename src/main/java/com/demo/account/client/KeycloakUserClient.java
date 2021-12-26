package com.demo.account.client;

import com.demo.account.model.keycloak.KeycloakUser;
import com.demo.account.model.keycloak.UserRepresentation;

import java.util.Optional;

public interface KeycloakUserClient {
    Optional<KeycloakUser> getUserByUsername(String username);

    void createAccount(KeycloakUser keycloakUser);

    <T extends UserRepresentation> void updateUser(String id, T userRepresentation);
}
