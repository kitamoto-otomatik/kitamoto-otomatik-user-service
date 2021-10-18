package com.demo.account.service;

import com.demo.account.model.AccessToken;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class KeycloakService {
    @Value("${keycloak.host}")
    private String host;

    @Value("${keycloak.token.endpoint}")
    private String tokenEndpoint;

    @Value("${keycloak.token.grant.type.key}")
    private String grantTypeKey;

    @Value("${keycloak.token.grant.type.value}")
    private String grantTypeValue;

    @Value("${keycloak.token.client.id.key}")
    private String clientIdKey;

    @Value("${keycloak.token.client.id.value}")
    private String clientIdValue;

    @Value("${keycloak.token.client.secret.key}")
    private String clientSecretKey;

    @Value("${keycloak.token.client.secret.value}")
    private String clientSecretValue;

    @Value("${keycloak.users.endpoint}")
    private String usersEndpoint;

    @Value("${keycloak.users.endpoint.username.query.param}")
    private String usernameQueryParam;

    public Optional<KeycloakUser> getUserByUsername(String username) {
        KeycloakUser[] keycloakUsers = WebClient.create(host)
                .get()
                .uri(e -> e.path(usersEndpoint)
                        .queryParam(usernameQueryParam, username)
                        .build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(getKeycloakToken()))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(KeycloakUser[].class);
                    } else {
                        return response.createException().flatMap(Mono::error);
                    }
                })
                .block();

        if (keycloakUsers != null && keycloakUsers.length == 1) {
            return Optional.of(keycloakUsers[0]);
        } else {
            return Optional.empty();
        }
    }

    private String getKeycloakToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(grantTypeKey, grantTypeValue);
        formData.add(clientIdKey, clientIdValue);
        formData.add(clientSecretKey, clientSecretValue);

        AccessToken accessToken = WebClient.create(host)
                .post()
                .uri(e -> e.path(tokenEndpoint).build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(AccessToken.class);
                    } else {
                        return response.createException().flatMap(Mono::error);
                    }
                })
                .block();

        if (null == accessToken) {
            throw new RuntimeException("Failed to get Keycloak token");
        }

        return accessToken.getAccessToken();
    }
}
