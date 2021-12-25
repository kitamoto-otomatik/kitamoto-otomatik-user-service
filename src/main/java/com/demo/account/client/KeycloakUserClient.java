package com.demo.account.client;

import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakResetPasswordRequest;
import com.demo.account.model.KeycloakUser;

import java.util.List;

public interface KeycloakUserClient {
    List<KeycloakUser> getUserListByUsername(String username);

    void createAccount(KeycloakUser keycloakUser);

    void activateAccount(String id, AccountActivationRequest accountActivationRequest);

    void updateKeycloakAccountAttribute(String id, KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest);

    void updateKeycloakAccountCredentials(String id, KeycloakResetPasswordRequest keycloakResetPasswordRequest);
}
