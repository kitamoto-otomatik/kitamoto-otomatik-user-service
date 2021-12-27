package com.demo.account.service;

import com.demo.account.model.AccountStatus;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class GetAccountStatusByUsernameService {
    @Autowired
    private KeycloakUserClient keycloakUserClient;

    public AccountStatus getAccountStatusByUsername(String username) {
        Optional<KeycloakUser> optionalKeycloakUser = keycloakUserClient.getUserByUsername(username);
        if (optionalKeycloakUser.isEmpty()) {
            return AccountStatus.UNREGISTERED;
        }

        KeycloakUser keycloakUser = optionalKeycloakUser.get();
        return keycloakUser.isEmailVerified() ? AccountStatus.ACTIVE : AccountStatus.UNVERIFIED;
    }
}
