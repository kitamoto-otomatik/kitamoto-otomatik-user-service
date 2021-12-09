package com.demo.account.model;

import lombok.Data;

@Data
public class AccountActivationRequest {
    private boolean emailVerified;
    private boolean enabled;
}
