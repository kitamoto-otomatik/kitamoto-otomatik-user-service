package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.Credential;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.demo.ErrorMessage.USERNAME_IS_ALREADY_TAKEN_ERROR_MESSAGE;

@Service
public class CreateAccountService {
    @Value("${account.activation.code}")
    private String accountActivationCode;

    @Autowired
    private GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    @Autowired
    private AccountActivationEmailService accountActivationEmailService;

    public void createAccount(CreateAccountRequest request) {
        AccountStatus accountStatus = getAccountStatusByUsernameService.getAccountStatusByUsername(request.getUsername());
        if (accountStatus != AccountStatus.UNREGISTERED) {
            throw new RequestException(USERNAME_IS_ALREADY_TAKEN_ERROR_MESSAGE);
        }

        keycloakUserClient.createAccount(transformToKeycloakUser(request));
        accountActivationEmailService.sendAccountActivationCode(request.getUsername());
    }

    private KeycloakUser transformToKeycloakUser(CreateAccountRequest request) {
        Credential credential = new Credential();
        credential.setType("password");
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setUsername(request.getUsername());
        keycloakUser.setEmail(request.getUsername());
        keycloakUser.setFirstName(request.getFirstName());
        keycloakUser.setLastName(request.getLastName());
        keycloakUser.setCredentials(Collections.singletonList(credential));
        return keycloakUser;
    }
}
