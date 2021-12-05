package com.demo.account.client;

import com.demo.account.model.KeycloakUser;

import java.util.List;

public interface KeycloakUserClient {
    List<KeycloakUser> getUserListByUsername(String username);

    void createAccount(KeycloakUser keycloakUser);
}
