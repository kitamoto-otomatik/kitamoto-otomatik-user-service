package com.demo.keycloak.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KeycloakResetPasswordRequest implements UserRepresentation {
    private Map<String, List<String>> attributes;
    private List<Credential> credentials;
}
