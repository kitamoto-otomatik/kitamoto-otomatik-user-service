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

import java.util.*;

import static com.demo.ErrorMessage.USERNAME_IS_ALREADY_TAKEN_ERROR_MESSAGE;

@Service
public class CreateAccountService {
    @Value("${account.activation.code}")
    private String code;

    @Autowired
    private GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    @Autowired
    private AccountActivationEmailService accountActivationEmailService;

    public void createAccount(CreateAccountRequest createAccountRequest) {
        AccountStatus accountStatus = getAccountStatusByUsernameService.getAccountStatusByUsername(createAccountRequest.getUsername());
        if (accountStatus != AccountStatus.UNREGISTERED) {
            throw new RequestException(USERNAME_IS_ALREADY_TAKEN_ERROR_MESSAGE);
        }

        String activationCode = String.valueOf(new Random().nextInt(1_000_000));
        KeycloakUser keycloakUser = transformToKeycloakUser(createAccountRequest, activationCode);
        keycloakUserClient.createAccount(keycloakUser);
        accountActivationEmailService.sendActivationCode(createAccountRequest.getUsername(), activationCode);
    }

    private KeycloakUser transformToKeycloakUser(CreateAccountRequest createAccountRequest, String activationCode) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(code, Collections.singletonList(activationCode));

        Credential credential = new Credential();
        credential.setType("password");
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
}
