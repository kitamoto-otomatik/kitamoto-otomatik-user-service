package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.account.model.UpdatePasswordRequest;
import com.demo.keycloak.client.KeycloakTokenClient;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.exception.AuthenticationException;
import com.demo.keycloak.model.Credential;
import com.demo.keycloak.model.KeycloakResetPasswordRequest;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.token.service.TokenDecoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.demo.ErrorMessage.*;

@Service
public class UpdatePasswordService {
    private static final String PASSWORD = "password";

    @Value("${password.reset.code}")
    private String passwordResetCode;

    @Autowired
    private TokenDecoder tokenDecoder;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    @Autowired
    private KeycloakTokenClient keycloakTokenClient;

    public void updatePassword(String token, UpdatePasswordRequest request) {
        String username = tokenDecoder.getSubject(token);

        Optional<KeycloakUser> optionalKeycloakUser = keycloakUserClient.getUserByUsername(username);
        if (!optionalKeycloakUser.isPresent()) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        }

        KeycloakUser keycloakUser = optionalKeycloakUser.get();
        if (!keycloakUser.isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);
        }

        if (StringUtils.isBlank(keycloakTokenClient.getKeycloakToken(username, request.getOldPassword()))) {
            throw new AuthenticationException(KEYCLOAK_TOKEN_ERROR_MESSAGE);
        }

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(passwordResetCode, new ArrayList<>());

        Credential credential = new Credential();
        credential.setType(PASSWORD);
        credential.setValue(request.getNewPassword());
        credential.setTemporary(false);

        KeycloakResetPasswordRequest keycloakResetPasswordRequest = new KeycloakResetPasswordRequest();
        keycloakResetPasswordRequest.setAttributes(attributes);
        keycloakResetPasswordRequest.setCredentials(Collections.singletonList(credential));
        keycloakUserClient.updateUser(keycloakUser.getId(), keycloakResetPasswordRequest);
    }
}
