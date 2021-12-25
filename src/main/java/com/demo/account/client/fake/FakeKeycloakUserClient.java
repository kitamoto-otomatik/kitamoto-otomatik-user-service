package com.demo.account.client.fake;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakResetPasswordRequest;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile("mock")
public class FakeKeycloakUserClient implements KeycloakUserClient {
    @Value("${account.activation.code}")
    private String accountActivationCode;

    @Value("${password.reset.code}")
    private String passwordResetCode;

    @Override
    public List<KeycloakUser> getUserListByUsername(String username) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(accountActivationCode, Collections.singletonList("1234"));
        attributes.put(passwordResetCode, Collections.singletonList("abc123"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        if ("nikkinicholas.romero@gmail.com".equals(username)) {
            keycloakUser1.setUsername(username);
            keycloakUser1.setEmailVerified(false);
        } else if ("sayin.leslieanne@gmail.com".equals(username)){
            keycloakUser1.setUsername(username);
            keycloakUser1.setEmailVerified(true);
        }
        keycloakUser1.setAttributes(attributes);
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        return keycloakUserList;
    }

    @Override
    public void createAccount(KeycloakUser keycloakUser) {
    }

    @Override
    public void activateAccount(String id, AccountActivationRequest accountActivationRequest) {
    }

    @Override
    public void updateKeycloakAccountAttribute(String id, KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest) {
    }

    @Override
    public void updateKeycloakAccountCredentials(String id, KeycloakResetPasswordRequest keycloakResetPasswordRequest) {
    }
}
