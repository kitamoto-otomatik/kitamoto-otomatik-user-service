package com.demo.keycloak.client.fake;

import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.keycloak.model.UserRepresentation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@Profile("mock")
public class FakeKeycloakUserClient implements KeycloakUserClient {
    @Override
    public Optional<KeycloakUser> getUserByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public void createAccount(KeycloakUser keycloakUser) {
    }

    @Override
    public <T extends UserRepresentation> void updateUser(String id, T userRepresentation) {
    }
}
