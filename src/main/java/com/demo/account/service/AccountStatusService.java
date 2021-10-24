package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountStatusService {
    private final KeycloakUserClient keycloakUserClient;

    @Autowired
    public AccountStatusService(KeycloakUserClient keycloakUserClient) {
        this.keycloakUserClient = keycloakUserClient;
    }

    public AccountStatus getAccountStatusByUsername(String username) {
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(username);

        if (keycloakUserList.size() == 0) {
            return AccountStatus.UNREGISTERED;
        } else if (keycloakUserList.size() > 1) {
            throw new KeycloakException("Found multiple users with the same username");
        } else if (keycloakUserList.get(0).isEmailVerified()) {
            return AccountStatus.ACTIVE;
        } else {
            return AccountStatus.UNVERIFIED;
        }
    }
}
