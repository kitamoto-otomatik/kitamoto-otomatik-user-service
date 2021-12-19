package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccountStatus;
import com.demo.account.model.KeycloakUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static com.demo.ErrorMessage.NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(new ArrayList<>());
        assertThat(target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com")).isEqualTo(AccountStatus.UNREGISTERED);
        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenMultipleMatchWasFound() {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        KeycloakUser keycloakUser3 = new KeycloakUser();
        keycloakUser3.setUsername("nikkinicholas.romero@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        keycloakUserList.add(keycloakUser3);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        KeycloakException keycloakException = assertThrows(KeycloakException.class, () -> target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com"));
        assertThat(keycloakException.getMessage()).isEqualTo(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);
        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenEmailIsActive() {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        assertThat(target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com")).isEqualTo(AccountStatus.ACTIVE);
        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
    }

    @Test
    public void getAccountStatusByUsername_whenEmailIsUnverified() {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(false);
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        assertThat(target.getAccountStatusByUsername("nikkinicholas.romero@gmail.com")).isEqualTo(AccountStatus.UNVERIFIED);
        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
    }
}
