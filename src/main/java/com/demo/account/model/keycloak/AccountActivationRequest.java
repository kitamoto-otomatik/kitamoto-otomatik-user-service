package com.demo.account.model.keycloak;

import lombok.Data;

@Data
public class AccountActivationRequest implements UserRepresentation {
    private boolean emailVerified;
    private boolean enabled;
}
