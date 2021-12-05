package com.demo.account.client;

import org.springframework.stereotype.Component;

@Component
public interface KeycloakTokenClient {
    String getKeycloakToken();
}
