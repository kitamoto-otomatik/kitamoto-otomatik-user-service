package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.demo.ErrorMessage.*;

@Slf4j
@Service
public class AccountActivationService {
    @Value("${account.activation.code}")
    private String code;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    public void activateAccount(String username, String activationCode) {
        List<KeycloakUser> keycloakUserList = keycloakUserClient.getUserListByUsername(username);
        keycloakUserList.removeIf(account -> !username.equals(account.getUsername()));

        if (CollectionUtils.isEmpty(keycloakUserList)) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        } else if (keycloakUserList.size() != 1) {
            log.error("{} - {}", NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE, username);
            throw new KeycloakException(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);
        } else if (keycloakUserList.get(0).isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);
        } else if (!keycloakUserList.get(0).getAttributes().get(code).get(0).equals(activationCode)) {
            throw new RequestException(ACTIVATION_CODE_IS_INCORRECT_ERROR_MESSAGE);
        } else {
            AccountActivationRequest accountActivationRequest = new AccountActivationRequest();
            accountActivationRequest.setEnabled(true);
            accountActivationRequest.setEmailVerified(true);
            keycloakUserClient.activateAccount(keycloakUserList.get(0).getId(), accountActivationRequest);
        }
    }
}
