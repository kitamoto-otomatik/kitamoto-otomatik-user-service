package com.demo.keycloak.model;

import lombok.Data;

@Data
public class AccountActivationRequest implements UserRepresentation {
    private boolean emailVerified;
    private boolean enabled;
}
