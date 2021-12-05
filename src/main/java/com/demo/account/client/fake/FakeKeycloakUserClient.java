package com.demo.account.client.fake;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.KeycloakUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("mock")
public class FakeKeycloakUserClient implements KeycloakUserClient {
    @Override
    public List<KeycloakUser> getUserListByUsername(String username) {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        return keycloakUserList;
    }

    @Override
    public void createAccount(KeycloakUser keycloakUser) {
    }
}
