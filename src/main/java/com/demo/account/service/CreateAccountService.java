package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.Credential;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

@Service
public class CreateAccountService {
    private static final String VERIFICATION_CODE = "verificationCode";
    private static final String PASSWORD = "password";
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
            Map<String, String> attributes = new HashMap<>();
            attributes.put(VERIFICATION_CODE, generateVerificationCode());

            Credential credential = new Credential();
            credential.setType(PASSWORD);
            credential.setValue(createAccountRequest.getPassword());
            credential.setTemporary(false);

            KeycloakUser keycloakUser = new KeycloakUser();
            keycloakUser.setUsername(createAccountRequest.getUsername());
            keycloakUser.setEmail(createAccountRequest.getUsername());
            keycloakUser.setFirstName(createAccountRequest.getFirstName());
            keycloakUser.setLastName(createAccountRequest.getLastName());
            keycloakUser.setAttributes(attributes);
            keycloakUser.setCredentials(Collections.singletonList(credential));
            return keycloakUser;
        };
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(1_000_000));
    }
}
