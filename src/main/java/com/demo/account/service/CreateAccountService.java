package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.Credential;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CreateAccountService {
    private static final String VERIFICATION_CODE = "verificationCode";
    private static final String PASSWORD = "password";
    private final KeycloakUserClient keycloakUserClient;
    private final AccountActivationEmailService accountActivationEmailService;

    @Autowired
    public CreateAccountService(KeycloakUserClient keycloakUserClient, AccountActivationEmailService accountActivationEmailService) {
        this.keycloakUserClient = keycloakUserClient;
        this.accountActivationEmailService = accountActivationEmailService;
    }

    public void createAccount(CreateAccountRequest createAccountRequest) {
        KeycloakUser keycloakUser = transformToKeycloakUser(createAccountRequest);
        keycloakUserClient.createAccount(keycloakUser);
        accountActivationEmailService.sendVerificationEmail(createAccountRequest.getUsername(), keycloakUser.getAttributes().get(VERIFICATION_CODE).get(0));
    }

    private KeycloakUser transformToKeycloakUser(CreateAccountRequest createAccountRequest) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(VERIFICATION_CODE, Collections.singletonList(generateVerificationCode()));

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
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(1_000_000));
    }
}
