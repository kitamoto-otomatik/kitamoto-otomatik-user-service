package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AccountStatusService {
    private final KeycloakUserClient keycloakUserClient;

    @Autowired
    public AccountStatusService(KeycloakUserClient keycloakUserClient) {
        this.keycloakUserClient = keycloakUserClient;
    }

    public Mono<AccountStatus> getAccountStatusByUsername(String username) {
        return keycloakUserClient.getUserListByUsername(username).map(e -> {
            if (CollectionUtils.isEmpty(e)) {
                return AccountStatus.UNREGISTERED;
            } else if (e.size() > 1) {
                throw new KeycloakException("Found multiple users with the same username");
            } else if (e.get(0).isEmailVerified()) {
                return AccountStatus.ACTIVE;
            } else {
                return AccountStatus.UNVERIFIED;
            }
        });
    }
}

