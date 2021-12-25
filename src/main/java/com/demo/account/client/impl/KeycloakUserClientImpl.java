package com.demo.account.client.impl;

import com.demo.account.client.KeycloakTokenClient;
import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakAccountAttributeUpdateRequest;
import com.demo.account.model.KeycloakResetPasswordRequest;
import com.demo.account.model.KeycloakUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.demo.ErrorMessage.*;

@Slf4j
@Component
@Profile("!mock")
public class KeycloakUserClientImpl implements KeycloakUserClient {
    @Value("${keycloak.host}")
    private String host;

    @Value("${keycloak.users.endpoint}")
    private String endpoint;

    @Autowired
    private KeycloakTokenClient tokenService;

    @Override
    public List<KeycloakUser> getUserListByUsername(String username) {
        return WebClient.builder()
                .baseUrl(host)
                .build()
                .get()
                .uri(e -> e.path(endpoint).queryParam("username", username).build())
                .headers(httpHeaders())
                .retrieve()
                .onStatus(HttpStatus::isError, errorHandler())
                .bodyToMono(KeycloakUser[].class)
                .doOnError(errorHandler(KEYCLOAK_GET_USER_ERROR_MESSAGE, username))
                .map(Arrays::asList)
                .block();
    }

    @Override
    public void createAccount(KeycloakUser keycloakUser) {
        WebClient.builder()
                .baseUrl(host)
                .build()
                .post()
                .uri(e -> e.path(endpoint).build())
                .headers(httpHeaders())
                .body(BodyInserters.fromValue(keycloakUser))
                .retrieve()
                .onStatus(HttpStatus::isError, errorHandler())
                .bodyToMono(Void.class)
                .doOnError(errorHandler(KEYCLOAK_USER_CREATION_ERROR_MESSAGE, keycloakUser.getUsername()))
                .block();
    }

    @Override
    public void activateAccount(String id, AccountActivationRequest accountActivationRequest) {
        WebClient.builder()
                .baseUrl(host)
                .build()
                .put()
                .uri(e -> e.path(endpoint + "/" + id).build())
                .headers(httpHeaders())
                .body(BodyInserters.fromValue(accountActivationRequest))
                .retrieve()
                .onStatus(HttpStatus::isError, errorHandler())
                .bodyToMono(Void.class)
                .doOnError(errorHandler(KEYCLOAK_USER_ACTIVATION_ERROR_MESSAGE, id))
                .block();
    }

    @Override
    public void updateKeycloakAccountAttribute(String id, KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest) {
        WebClient.builder()
                .baseUrl(host)
                .build()
                .put()
                .uri(e -> e.path(endpoint + "/" + id).build())
                .headers(httpHeaders())
                .body(BodyInserters.fromValue(keycloakAccountAttributeUpdateRequest))
                .retrieve()
                .onStatus(HttpStatus::isError, errorHandler())
                .bodyToMono(Void.class)
                .doOnError(errorHandler(KEYCLOAK_USER_UPDATE_ERROR_MESSAGE, id))
                .block();
    }

    @Override
    public void updateKeycloakAccountCredentials(String id, KeycloakResetPasswordRequest keycloakResetPasswordRequest) {
        WebClient.builder()
                .baseUrl(host)
                .build()
                .put()
                .uri(e -> e.path(endpoint + "/" + id).build())
                .headers(httpHeaders())
                .body(BodyInserters.fromValue(keycloakResetPasswordRequest))
                .retrieve()
                .onStatus(HttpStatus::isError, errorHandler())
                .bodyToMono(Void.class)
                .doOnError(errorHandler(KEYCLOAK_USER_UPDATE_ERROR_MESSAGE, id))
                .block();
    }

    private Consumer<HttpHeaders> httpHeaders() {
        return httpHeaders -> httpHeaders.setBearerAuth(tokenService.getKeycloakToken());
    }

    private Function<ClientResponse, Mono<? extends Throwable>> errorHandler() {
        return response -> response
                .createException()
                .map(e -> new RuntimeException(e.getResponseBodyAsString()));
    }

    private Consumer<Throwable> errorHandler(String message, String id) {
        return e -> {
            log.error("{} - {}", message, id, e);
            throw new KeycloakException(message);
        };
    }
}
