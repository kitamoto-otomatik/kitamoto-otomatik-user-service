package com.demo.account;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.KeycloakUser;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class Service {
    private static final String ERROR_MESSAGE = "Found multiple users with the same username";

    private final KeycloakUserClient keycloakUserClient;

    @Autowired
    public Service(KeycloakUserClient keycloakUserClient) {
        this.keycloakUserClient = keycloakUserClient;
    }

    public Mono<AccountStatus> getAccountStatusByUsername(String username) {
        return keycloakUserClient.getUserListByUsername(username).map(e -> {
            List<KeycloakUser> keycloakUserList = new ArrayList<>(e);
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
        });
    }
}

