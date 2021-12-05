package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.KeycloakUser;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAccountStatusByUsernameService {
    private static final String ERROR_MESSAGE = "Found multiple users with the same username";

    private final KeycloakUserClient keycloakUserClient;

    @Autowired
    public GetAccountStatusByUsernameService(KeycloakUserClient keycloakUserClient) {
        this.keycloakUserClient = keycloakUserClient;
    }

    public AccountStatus getAccountStatusByUsername(String username) {
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(username);
        keycloakUserList.removeIf(account -> !username.equals(account.getUsername()));

        if (CollectionUtils.isEmpty(keycloakUserList)) {
            return AccountStatus.UNREGISTERED;
        } else if (keycloakUserList.size() > 1) {
            throw new KeycloakException(ERROR_MESSAGE);
        } else if (keycloakUserList.get(0).isEmailVerified()) {
            return AccountStatus.ACTIVE;
        } else {
            return AccountStatus.UNVERIFIED;
        }
    }
}
