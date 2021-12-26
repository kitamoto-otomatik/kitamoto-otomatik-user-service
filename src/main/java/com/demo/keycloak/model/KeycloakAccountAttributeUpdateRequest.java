package com.demo.keycloak.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KeycloakAccountAttributeUpdateRequest implements UserRepresentation {
    private Map<String, List<String>> attributes;
}
