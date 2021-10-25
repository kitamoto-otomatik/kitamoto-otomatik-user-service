package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.KeycloakUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateAccountServiceTest {
    @InjectMocks
    private CreateAccountService target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Captor
    private ArgumentCaptor<KeycloakUser> argumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createAccount() {
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        when(keycloakUserClient.createAccount(any(KeycloakUser.class))).thenReturn(Mono.empty());

        CreateAccountRequest createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setUsername("nikkinicholas.romero@gmail.com");
        createAccountRequest.setFirstName("Nikki Nicholas");
        createAccountRequest.setLastName("Romero");

        Mono<Void> actual = target.createAccount(Mono.just(createAccountRequest));
        assertThat(actual).isNotNull();

        StepVerifier.create(actual.log())
                .verifyComplete();

        verify(keycloakUserClient).createAccount(argumentCaptor.capture());
        KeycloakUser keycloakUser = argumentCaptor.getValue();
        assertThat(keycloakUser).isNotNull();
        assertThat(keycloakUser.getUsername()).isEqualTo("nikkinicholas.romero@gmail.com");
        assertThat(keycloakUser.getEmail()).isEqualTo("nikkinicholas.romero@gmail.com");
        assertThat(keycloakUser.getFirstName()).isEqualTo("Nikki Nicholas");
        assertThat(keycloakUser.getLastName()).isEqualTo("Romero");
    }
}
