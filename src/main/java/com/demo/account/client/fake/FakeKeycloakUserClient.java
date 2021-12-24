package com.demo.account.client.fake;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile("mock")
public class FakeKeycloakUserClient implements KeycloakUserClient {
    @Value("${account.activation.code}")
    private String code;

    @Override
    public List<KeycloakUser> getUserListByUsername(String username) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(code, Collections.singletonList("1234"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(false);
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
}
