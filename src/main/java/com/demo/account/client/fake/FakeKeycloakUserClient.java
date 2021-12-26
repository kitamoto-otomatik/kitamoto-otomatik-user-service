package com.demo.account.client.fake;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakResetPasswordRequest;
import com.demo.account.model.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@Profile("mock")
public class FakeKeycloakUserClient implements KeycloakUserClient {
    @Override
    public Optional<KeycloakUser> getUserByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public void createAccount(KeycloakUser keycloakUser) {
    }

    @Override
    public void activateAccount(String id, AccountActivationRequest accountActivationRequest) {
    }

    @Override
    public void updateKeycloakAccountAttribute(String id, KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest) {
    }

    @Override
    public void updateKeycloakAccountCredentials(String id, KeycloakResetPasswordRequest keycloakResetPasswordRequest) {
    }
}
