package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.account.model.UpdateProfileRequest;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.token.service.TokenDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.demo.ErrorMessage.ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE;
import static com.demo.ErrorMessage.USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE;

@Service
public class UpdateProfileService {
    @Autowired
    private TokenDecoder tokenDecoder;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    public void updateProfile(String token, UpdateProfileRequest request) {
        String username = tokenDecoder.getSubject(token);

        Optional<KeycloakUser> optionalKeycloakUser = keycloakUserClient.getUserByUsername(username);
        if (optionalKeycloakUser.isEmpty()) {
            throw new RequestException(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);
        }

        KeycloakUser keycloakUser = optionalKeycloakUser.get();
        if (!keycloakUser.isEmailVerified()) {
            throw new RequestException(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);
        }

        keycloakUserClient.updateUser(keycloakUser.getId(), request);
    }
}
