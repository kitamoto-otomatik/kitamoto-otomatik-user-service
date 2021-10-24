package com.demo.account;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.KeycloakUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceTest {
    @InjectMocks
    private Service target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAccountStatusByUsername_whenUnregistered() {
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(Mono.just(keycloakUserList));

        Mono<AccountStatus> actual = target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com");
        assertThat(actual).isNotNull();

        StepVerifier.create(actual.log())
                .expectNext(AccountStatus.UNREGISTERED)
                .verifyComplete();

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");

    }

    @Test
    public void getAccountStatusByUsername_whenFoundMoreThanOneAccount() {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setEmailVerified(true);
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setEmailVerified(true);
        keycloakUser2.setUsername("nikkinicholas.romero@gmail.com");
        List<KeycloakUser> keycloakUserList = Arrays.asList(keycloakUser1, keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(Mono.just(keycloakUserList));

        Mono<AccountStatus> actual = target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com");

        StepVerifier.create(actual.log())
                .expectError(KeycloakException.class)
                .verify();

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenActive() {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setEmailVerified(true);
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setEmailVerified(true);
        keycloakUser2.setUsername("nikkinicholas.romero@yahoo.com");
        List<KeycloakUser> keycloakUserList = Arrays.asList(keycloakUser1, keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(Mono.just(keycloakUserList));

        Mono<AccountStatus> actual = target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com");

        StepVerifier.create(actual.log())
                .expectNext(AccountStatus.ACTIVE)
                .verifyComplete();

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenUnverified() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setEmailVerified(false);
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        List<KeycloakUser> keycloakUserList = Arrays.asList(keycloakUser);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(Mono.just(keycloakUserList));

        Mono<AccountStatus> actual = target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com");

        StepVerifier.create(actual.log())
                .expectNext(AccountStatus.UNVERIFIED)
                .verifyComplete();

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
    }
}
