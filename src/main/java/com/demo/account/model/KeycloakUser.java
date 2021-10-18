package com.demo.account.model;

import lombok.Data;

@Data
public class KeycloakUser {
    private String username;
    private boolean emailVerified;
}
