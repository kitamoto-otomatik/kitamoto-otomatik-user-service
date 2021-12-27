package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.account.model.UpdateProfileRequest;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.token.service.TokenDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.demo.ErrorMessage.ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE;
import static com.demo.ErrorMessage.USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UpdateProfileServiceTest {
    private static final String USERNAME = "nikkinicholas.romero@gmail.com";
    private static final String TOKEN = "some_token";

    @InjectMocks
    private UpdateProfileService target;

    @Mock
    private TokenDecoder tokenDecoder;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    private UpdateProfileRequest request;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        request = new UpdateProfileRequest();
        request.setFirstName("Nikki Nicholas");
        request.setLastName("Romero");

        when(tokenDecoder.getSubject(anyString())).thenReturn(USERNAME);
    }

    @Test
    public void updateProfile_whenUserDoesNotExist() {
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.empty());

        RequestException e = assertThrows(RequestException.class, () -> target.updateProfile(TOKEN, request));
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);

        verify(tokenDecoder).getSubject(TOKEN);
        verify(keycloakUserClient, never()).updateUser(anyString(), any());
    }

    @Test
    public void updateProfile_whenUserIsNotYetActivated() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setEmailVerified(false);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () -> target.updateProfile(TOKEN, request));
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);

        verify(tokenDecoder).getSubject(TOKEN);
        verify(keycloakUserClient, never()).updateUser(anyString(), any());
    }

    @Test
    public void updateProfile() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setEmailVerified(true);
        keycloakUser.setId("some_id");
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        target.updateProfile(TOKEN, request);

        verify(tokenDecoder).getSubject(TOKEN);
        verify(keycloakUserClient).updateUser(keycloakUser.getId(), request);
    }
}
