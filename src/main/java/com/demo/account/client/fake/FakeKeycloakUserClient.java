package com.demo.account.client.fake;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile("mock")
public class FakeKeycloakUserClient implements KeycloakUserClient {
    private static final String VERIFICATION_CODE = "verificationCode";

    @Override
    public List<KeycloakUser> getUserListByUsername(String username) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(VERIFICATION_CODE, Collections.singletonList("1234"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
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
}
