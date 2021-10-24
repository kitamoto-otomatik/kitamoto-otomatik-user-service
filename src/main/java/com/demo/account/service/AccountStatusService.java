package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountStatusService {
    @Autowired
    private KeycloakUserClient keycloakUserClient;

    public AccountStatus getAccountStatusByUsername(String username) {
        Optional<List<KeycloakUser>> optionalKeycloakUserList = keycloakUserClient.getUserListByUsername(username);
        if (optionalKeycloakUserList.isPresent()) {
            List<KeycloakUser> keycloakUserList = optionalKeycloakUserList.get();
            if (keycloakUserList.size() != 1) {
                throw new KeycloakException("Application is invalid state");
            }

            if (keycloakUserList.get(0).isEmailVerified()) {
                return AccountStatus.ACTIVE;
            } else {
                return AccountStatus.UNVERIFIED;
            }
        } else {
            return AccountStatus.UNREGISTERED;
        }
    }
}
