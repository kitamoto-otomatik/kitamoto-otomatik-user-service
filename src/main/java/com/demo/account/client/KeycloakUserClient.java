package com.demo.account.client;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class KeycloakUserClient {
    private final KeycloakTokenClient tokenService;

    @Value("${keycloak.host}")
    private String host;

    @Value("${keycloak.users.endpoint}")
    private String usersEndpoint;

    @Autowired
    public KeycloakUserClient(KeycloakTokenClient tokenService) {
        this.tokenService = tokenService;
    }

    public Mono<List<KeycloakUser>> getUserListByUsername(String username) {
        return tokenService.getKeycloakToken()
                .flatMap(token -> WebClient.builder()
                        .baseUrl(host)
                        .build()
                        .get()
                        .uri(e -> e.path(usersEndpoint).queryParam("username", username).build())
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                        .retrieve()
                        .onStatus(HttpStatus::isError, response -> Mono.error(new KeycloakException("Could not get Keycloak users")))
                        .bodyToMono(KeycloakUser[].class)
                        .doOnError(e -> {
                            throw new KeycloakException("Could not get Keycloak users");
                        })
                        .map(Arrays::asList));
    }
}
