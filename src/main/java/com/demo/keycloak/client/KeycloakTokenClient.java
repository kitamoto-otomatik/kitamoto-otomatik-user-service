package com.demo.keycloak.client;

public interface KeycloakTokenClient {
    String getKeycloakToken();

    String getKeycloakToken(String username, String password);
}
