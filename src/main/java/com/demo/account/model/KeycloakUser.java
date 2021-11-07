package com.demo.account.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KeycloakUser {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Map<String, String> attributes;
    private List<Credential> credentials;
    private boolean emailVerified;
    private boolean enabled;
}
