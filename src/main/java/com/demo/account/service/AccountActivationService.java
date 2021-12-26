package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.demo.ErrorMessage.*;

@Slf4j
@Service
public class AccountActivationService {
    @Value("${account.activation.code}")
    private String accountActivationCode;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    public void activateAccount(String username, String activationCode) {
        Optional<KeycloakUser> optionalKeycloakUser = keycloakUserClient.getUserByUsername(username);

        if (!optionalKeycloakUser.isPresent()) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        }

        KeycloakUser keycloakUser = optionalKeycloakUser.get();
        if (keycloakUser.isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);
        }

        if (MapUtils.isEmpty(keycloakUser.getAttributes()) ||
                !keycloakUser.getAttributes().containsKey(accountActivationCode) ||
                CollectionUtils.isEmpty(keycloakUser.getAttributes().get(accountActivationCode)) ||
                StringUtils.isEmpty(keycloakUser.getAttributes().get(accountActivationCode).get(0)) ||
                !keycloakUser.getAttributes().get(accountActivationCode).get(0).equals(activationCode)) {
            throw new RequestException(ACTIVATION_CODE_IS_INCORRECT_ERROR_MESSAGE);
        }

        AccountActivationRequest accountActivationRequest = new AccountActivationRequest();
        accountActivationRequest.setEnabled(true);
        accountActivationRequest.setEmailVerified(true);
        keycloakUserClient.activateAccount(keycloakUser.getId(), accountActivationRequest);
    }
}
