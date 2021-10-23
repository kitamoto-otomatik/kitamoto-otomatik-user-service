package com.demo.account.service;

import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class KeycloakUserClient {
    private final KeycloakTokenClient tokenService;

    @Value("${keycloak.host}")
    private String host;

    @Value("${keycloak.users.endpoint}")
    private String usersEndpoint;

    @Value("${keycloak.users.endpoint.username.query.param}")
    private String usernameQueryParam;

    @Autowired
    public KeycloakUserClient(KeycloakTokenClient tokenService) {
        this.tokenService = tokenService;
    }

    public Optional<List<KeycloakUser>> getUserListByUsername(String username) {
        Function<UriBuilder, URI> uriFunction = e -> e.path(usersEndpoint)
                .queryParam(usernameQueryParam, username)
                .build();

        Consumer<HttpHeaders> httpHeadersConsumer = httpHeaders -> httpHeaders.setBearerAuth(tokenService.getKeycloakToken().orElseThrow(RuntimeException::new));

        Function<ClientResponse, Mono<KeycloakUser[]>> clientResponseMonoFunction = response -> {
            if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(KeycloakUser[].class);
            } else {
                return response.createException().flatMap(Mono::error);
            }
        };

        KeycloakUser[] keycloakUsers = WebClient.builder().baseUrl(host).build()
                .get()
                .uri(uriFunction)
                .headers(httpHeadersConsumer)
                .exchangeToMono(clientResponseMonoFunction)
                .block();

        return null == keycloakUsers ? Optional.empty() : Optional.of(Arrays.asList(keycloakUsers));
    }
}
