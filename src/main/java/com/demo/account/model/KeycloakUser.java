package com.demo.account.model;

import lombok.Data;

@Data
public class KeycloakUser {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean emailVerified;
}
