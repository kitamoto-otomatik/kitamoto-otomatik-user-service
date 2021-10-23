package com.demo.account.service;

import com.demo.account.model.AccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

@Component
public class KeycloakTokenClient {
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

    public KeycloakTokenClient() {
    }

    public Optional<String> getKeycloakToken() {
        Function<UriBuilder, URI> uriBuilderURIFunction = e -> e.path(tokenEndpoint).build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(grantTypeKey, grantTypeValue);
        formData.add(clientIdKey, clientIdValue);
        formData.add(clientSecretKey, clientSecretValue);

        Function<ClientResponse, Mono<AccessToken>> clientResponseMonoFunction = response -> {
            if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(AccessToken.class);
            } else {
                return response.createException().flatMap(Mono::error);
            }
        };

        AccessToken accessToken = WebClient.builder().baseUrl(host).build()
                .post()
                .uri(uriBuilderURIFunction)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchangeToMono(clientResponseMonoFunction)
                .block();

        return null == accessToken ? Optional.empty() : Optional.of(accessToken.getAccessToken());
    }
}
