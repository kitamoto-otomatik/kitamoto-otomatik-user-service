package com.demo.keycloak.client.impl;

import com.demo.keycloak.client.KeycloakTokenClient;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.exception.KeycloakException;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.keycloak.model.UserRepresentation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.Optional;
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
    public Optional<KeycloakUser> getUserByUsername(String username) {
        List<KeycloakUser> keycloakUserList = WebClient.builder()
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

        return CollectionUtils.isEmpty(keycloakUserList) ?
                Optional.empty() : keycloakUserList.stream()
                .filter(e -> e.getUsername().equals(username))
                .findFirst();
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
    public <T extends UserRepresentation> void updateUser(String id, T userRepresentation) {
        WebClient.builder()
                .baseUrl(host)
                .build()
                .put()
                .uri(e -> e.path(endpoint + "/" + id).build())
                .headers(httpHeaders())
                .body(BodyInserters.fromValue(userRepresentation))
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
