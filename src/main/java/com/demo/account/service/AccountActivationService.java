package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountActivationService {
    private static final String MULTIPLE_MATCHING_USERNAME_FOUND = "Multiple matching username found";
    private static final String ACCOUNT_IS_ALREADY_ACTIVATED = "Account is already activated";
    private static final String ACCOUNT_ACTIVATION_IS_INCORRECT = "Account activation is incorrect";
    private static final String VERIFICATION_CODE = "verificationCode";
    private final KeycloakUserClient keycloakUserClient;

    @Autowired
    public AccountActivationService(KeycloakUserClient keycloakUserClient) {
        this.keycloakUserClient = keycloakUserClient;
    }

    public void activateAccount(String emailAddress, String activationCode) {
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(emailAddress);
        keycloakUserList.removeIf(account -> !emailAddress.equals(account.getUsername()));

        if (keycloakUserList.size() != 1) {
            throw new KeycloakException(MULTIPLE_MATCHING_USERNAME_FOUND);
        }

        KeycloakUser keycloakUser = keycloakUserList.get(0);
        if (keycloakUser.isEmailVerified()) {
            throw new KeycloakException(ACCOUNT_IS_ALREADY_ACTIVATED);
        }

        if (!keycloakUser.getAttributes().get(VERIFICATION_CODE).get(0).equals(activationCode)) {
            throw new KeycloakException(ACCOUNT_ACTIVATION_IS_INCORRECT);
        }

        AccountActivationRequest accountActivationRequest = new AccountActivationRequest();
        accountActivationRequest.setEnabled(true);
        accountActivationRequest.setEmailVerified(true);
        keycloakUserClient.activateAccount(keycloakUserList.get(0).getId(), accountActivationRequest);
    }
}
