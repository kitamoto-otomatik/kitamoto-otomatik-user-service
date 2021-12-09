package com.demo.account.client.impl;

import com.demo.account.client.KeycloakTokenClient;
import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakErrorResponse;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("!mock")
public class KeycloakUserClientImpl implements KeycloakUserClient {
    private static final String GET_ACCOUNT_ERROR_MESSAGE = "Could not get Keycloak users";
    private final KeycloakTokenClient tokenService;

    @Value("${keycloak.host}")
    private String host;

    @Value("${keycloak.users.endpoint}")
    private String usersEndpoint;

    @Autowired
    public KeycloakUserClientImpl(KeycloakTokenClient tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public List<KeycloakUser> getUserListByUsername(String username) {
        return WebClient.builder()
                .baseUrl(host)
                .build()
                .get()
                .uri(e -> e.path(usersEndpoint).queryParam("username", username).build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(tokenService.getKeycloakToken()))
                .retrieve()
                .onStatus(HttpStatus::isError, response -> Mono.error(new KeycloakException(GET_ACCOUNT_ERROR_MESSAGE)))
                .bodyToMono(KeycloakUser[].class)
                .doOnError(e -> {
                    throw new KeycloakException(e.getMessage());
                })
                .map(Arrays::asList)
                .block();
    }

    @Override
    public void createAccount(KeycloakUser keycloakUser) {
        WebClient.builder()
                .baseUrl(host)
                .build()
                .post()
                .uri(e -> e.path(usersEndpoint).build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(tokenService.getKeycloakToken()))
                .body(BodyInserters.fromValue(keycloakUser))
                .retrieve()
                .onStatus(HttpStatus::isError, response -> response.bodyToMono(KeycloakErrorResponse.class).map(body -> new KeycloakException(body.getErrorMessage())))
                .bodyToMono(Void.class)
                .doOnError(e -> {
                    throw new KeycloakException(e.getMessage());
                })
                .block();
    }

    @Override
    public void activateAccount(String id, AccountActivationRequest accountActivationRequest) {
        WebClient.builder()
                .baseUrl(host)
                .build()
                .put()
                .uri(e -> e.path(usersEndpoint + "/" + id).build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(tokenService.getKeycloakToken()))
                .body(BodyInserters.fromValue(accountActivationRequest))
                .retrieve()
                .onStatus(HttpStatus::isError, response -> response.bodyToMono(KeycloakErrorResponse.class).map(body -> new KeycloakException(body.getErrorMessage())))
                .bodyToMono(Void.class)
                .doOnError(e -> {
                    throw new KeycloakException(e.getMessage());
                })
                .block();
    }
}
