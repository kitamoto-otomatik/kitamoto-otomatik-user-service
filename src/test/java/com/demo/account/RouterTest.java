package com.demo.account;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.AccountStatusResponse;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.ErrorResponse;
import com.demo.account.service.CreateAccountService;
import com.demo.account.service.GetAccountStatusByUsernameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RouterTest {
    @MockBean
    public GetAccountStatusByUsernameService getAccountStatusByUsernameService;

    @MockBean
    private CreateAccountService createAccountService;

    @Autowired
    private WebTestClient webTestClient;

    @Captor
    private ArgumentCaptor<Mono<CreateAccountRequest>> argumentCaptor;

    @Test
    public void getAccountStatusByUsername_whenActive() {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString())).thenReturn(Mono.just(AccountStatus.ACTIVE));

        webTestClient
                .get()
                .uri("/accounts/nikkinicholas.romero@gmail.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountStatusResponse.class).value(response -> {
                    assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
                });

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenError() {
        when(getAccountStatusByUsernameService.getAccountStatusByUsername(anyString())).thenReturn(Mono.error(new KeycloakException("Some Keycloak error")));

        webTestClient
                .get()
                .uri("/accounts/nikkinicholas.romero@gmail.com")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ErrorResponse.class).value(response -> {
                    assertThat(response.getCode()).isEqualTo("KeycloakException");
                    assertThat(response.getMessage()).isEqualTo("Some Keycloak error");
                });

        verify(getAccountStatusByUsernameService).getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void createAccount_whenSuccess() {
        when(createAccountService.createAccount(any())).thenReturn(Mono.empty());

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setUsername("nikkinicholas.romero@gmail.com");
        createAccountRequest.setFirstName("Nikki Nicholas");
        createAccountRequest.setLastName("Romero");

        webTestClient
                .post()
                .uri("/accounts")
                .body(BodyInserters.fromValue(createAccountRequest))
                .exchange()
                .expectStatus().isOk();

        verify(createAccountService).createAccount(argumentCaptor.capture());

        StepVerifier.create(argumentCaptor.getValue().log())
                .expectComplete();

        // TODO : Verify correct data was passed to createAccountService
    }

    @Test
    public void createAccount_whenError() {
        when(createAccountService.createAccount(any())).thenReturn(Mono.error(new KeycloakException("Some Keycloak error")));

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setUsername("nikkinicholas.romero@gmail.com");
        createAccountRequest.setFirstName("Nikki Nicholas");
        createAccountRequest.setLastName("Romero");

        webTestClient
                .post()
                .uri("/accounts")
                .body(BodyInserters.fromValue(createAccountRequest))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ErrorResponse.class).value(response -> {
                    assertThat(response.getCode()).isEqualTo("KeycloakException");
                    assertThat(response.getMessage()).isEqualTo("Some Keycloak error");
                });

        // TODO : Verify correct data was passed to createAccountService
    }
}
