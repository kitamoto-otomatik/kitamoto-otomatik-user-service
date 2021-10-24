package com.demo.account;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.AccountStatusResponse;
import com.demo.account.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RouterTest {
    @MockBean
    public Service service;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getAccountStatusByUsername_whenActive() {
        when(service.getAccountStatusByUsername(anyString())).thenReturn(Mono.just(AccountStatus.ACTIVE));

        webTestClient
                .get()
                .uri("/accounts/nikkinicholas.romero@gmail.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountStatusResponse.class).value(response -> {
                    assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
                });

        verify(service).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenError() {
        when(service.getAccountStatusByUsername(anyString())).thenReturn(Mono.error(new KeycloakException("Some Keycloak error")));

        webTestClient
                .get()
                .uri("/accounts/nikkinicholas.romero@gmail.com")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ErrorResponse.class).value(response -> {
                    assertThat(response.getCode()).isEqualTo("KeycloakException");
                    assertThat(response.getMessage()).isEqualTo("Some Keycloak error");
                });

        verify(service).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }
}
