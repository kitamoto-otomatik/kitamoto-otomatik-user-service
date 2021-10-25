package com.demo.account.client;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
public class KeycloakUserClient {
    private static final String GET_ACCOUNT_ERROR_MESSAGE = "Could not get Keycloak users";
    private static final String CREATE_ACCOUNT_ERROR_MESSAGE = "Could not create Keycloak user";
    private final KeycloakTokenClient tokenService;
    private WebClient webClient;

    @Value("${keycloak.host}")
    private String host;

    @Value("${keycloak.users.endpoint}")
    private String usersEndpoint;

    @Autowired
    public KeycloakUserClient(KeycloakTokenClient tokenService) {
        this.tokenService = tokenService;
    }

    @PostConstruct
    public void postConstruct() {
        this.webClient = WebClient.builder()
                .baseUrl(host)
                .build();
    }

    public Mono<List<KeycloakUser>> getUserListByUsername(String username) {
        return tokenService.getKeycloakToken().flatMap(token -> webClient
                .get()
                .uri(e -> e.path(usersEndpoint).queryParam("username", username).build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .onStatus(HttpStatus::isError, response -> Mono.error(new KeycloakException(GET_ACCOUNT_ERROR_MESSAGE)))
                .bodyToMono(KeycloakUser[].class)
                .doOnError(e -> {
                    throw new KeycloakException(GET_ACCOUNT_ERROR_MESSAGE);
                })
                .map(Arrays::asList));
    }

    public Mono<Void> createAccount(KeycloakUser keycloakUser) {
        return tokenService.getKeycloakToken().flatMap(token -> webClient
                .post()
                .uri(e -> e.path(usersEndpoint).build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .body(BodyInserters.fromValue(keycloakUser))
                .retrieve()
                .onStatus(HttpStatus::isError, response -> Mono.error(new KeycloakException(CREATE_ACCOUNT_ERROR_MESSAGE)))
                .bodyToMono(Void.class)
                .doOnError(e -> {
                    throw new KeycloakException(CREATE_ACCOUNT_ERROR_MESSAGE);
                }));
    }
}
