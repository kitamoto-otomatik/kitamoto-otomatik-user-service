package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
public class CreateAccountService {
    private final KeycloakUserClient keycloakUserClient;

    @Autowired
    public CreateAccountService(KeycloakUserClient keycloakUserClient) {
        this.keycloakUserClient = keycloakUserClient;
    }

    // TODO : Add validation
    // TODO : Persist verification code
    // TODO : Send email verification on success
    public Mono<Void> createAccount(Mono<CreateAccountRequest> createAccountRequestMono) {
        return createAccountRequestMono
                .map(mapToKeycloakUser())
                .flatMap(keycloakUserClient::createAccount);
    }

    private Function<CreateAccountRequest, KeycloakUser> mapToKeycloakUser() {
        return createAccountRequest -> {
            KeycloakUser keycloakUser = new KeycloakUser();
            keycloakUser.setUsername(createAccountRequest.getUsername());
            keycloakUser.setEmail(createAccountRequest.getUsername());
            keycloakUser.setFirstName(createAccountRequest.getFirstName());
            keycloakUser.setLastName(createAccountRequest.getLastName());
            return keycloakUser;
        };
    }
}
