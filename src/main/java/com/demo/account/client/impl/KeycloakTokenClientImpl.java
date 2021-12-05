package com.demo.account.client.impl;

import com.demo.account.client.KeycloakTokenClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Profile("!mock")
public class KeycloakTokenClientImpl implements KeycloakTokenClient {
    private static final String ERROR_MESSAGE = "Could not get Keycloak access token";

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

    @Override
    public String getKeycloakToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(grantTypeKey, grantTypeValue);
        formData.add(clientIdKey, clientIdValue);
        formData.add(clientSecretKey, clientSecretValue);

        return WebClient.builder()
                .baseUrl(host)
                .build()
                .post()
                .uri(e -> e.path(tokenEndpoint).build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(HttpStatus::isError, response -> Mono.error(new KeycloakException(ERROR_MESSAGE)))
                .bodyToMono(AccessToken.class)
                .doOnError(e -> {
                    throw new KeycloakException(ERROR_MESSAGE);
                })
                .map(AccessToken::getAccessToken)
                .block();
    }
}
