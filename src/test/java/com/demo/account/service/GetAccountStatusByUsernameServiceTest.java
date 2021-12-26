package com.demo.account.service;

import com.demo.account.model.AccountStatus;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.KeycloakUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetAccountStatusByUsernameServiceTest {
    @InjectMocks
    private GetAccountStatusByUsernameService target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAccountStatusByUsername_whenUnregistered() {
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.empty());
        assertThat(target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com")).isEqualTo(AccountStatus.UNREGISTERED);
        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenEmailIsActive() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setEmailVerified(true);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        assertThat(target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com")).isEqualTo(AccountStatus.ACTIVE);
        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenEmailIsUnverified() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setEmailVerified(false);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        assertThat(target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com")).isEqualTo(AccountStatus.UNVERIFIED);
        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
    }
}
