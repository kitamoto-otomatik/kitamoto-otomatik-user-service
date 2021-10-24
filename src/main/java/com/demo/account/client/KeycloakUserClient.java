package com.demo.account.client;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.KeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        try {
            KeycloakUser[] keycloakUsers = WebClient.builder().baseUrl(host).build()
                    .get()
                    .uri(e -> e.path(usersEndpoint).queryParam(usernameQueryParam, username).build())
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(tokenService.getKeycloakToken().orElseThrow(RuntimeException::new)))
                    .retrieve()
                    .onStatus(HttpStatus::isError, response -> Mono.error(new KeycloakException("Could not get Keycloak users")))
                    .bodyToMono(KeycloakUser[].class)
                    .block();

            return null == keycloakUsers ? Optional.empty() : Optional.of(Arrays.asList(keycloakUsers));
        } catch (WebClientRequestException e) {
            throw new KeycloakException("Could not get Keycloak users");
        }
    }
}
