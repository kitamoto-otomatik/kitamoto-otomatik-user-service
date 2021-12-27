package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.account.model.ResetPasswordRequest;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.Credential;
import com.demo.keycloak.model.KeycloakResetPasswordRequest;
import com.demo.keycloak.model.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.demo.ErrorMessage.*;

@Slf4j
@Service
public class ResetPasswordService {
    private static final String PASSWORD = "password";

    @Value("${password.reset.code}")
    private String passwordResetCode;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    public void resetPassword(ResetPasswordRequest request) {
        Optional<KeycloakUser> optionalKeycloakUser = keycloakUserClient.getUserByUsername(request.getUsername());
        if (optionalKeycloakUser.isEmpty()) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        }

        KeycloakUser keycloakUser = optionalKeycloakUser.get();
        if (!keycloakUser.isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);
        }

        if (MapUtils.isEmpty(keycloakUser.getAttributes()) ||
                !keycloakUser.getAttributes().containsKey(passwordResetCode) ||
                CollectionUtils.isEmpty(keycloakUser.getAttributes().get(passwordResetCode)) ||
                StringUtils.isEmpty(keycloakUser.getAttributes().get(passwordResetCode).get(0)) ||
                !keycloakUser.getAttributes().get(passwordResetCode).get(0).equals(request.getPasswordResetCode())) {
            throw new RequestException(PASSWORD_RESET_CODE_INVALID_ERROR_MESSAGE);
        }

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(passwordResetCode, new ArrayList<>());

        Credential credential = new Credential();
        credential.setType(PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        KeycloakResetPasswordRequest keycloakResetPasswordRequest = new KeycloakResetPasswordRequest();
        keycloakResetPasswordRequest.setAttributes(attributes);
        keycloakResetPasswordRequest.setCredentials(Collections.singletonList(credential));
        keycloakUserClient.updateUser(keycloakUser.getId(), keycloakResetPasswordRequest);
    }
}
