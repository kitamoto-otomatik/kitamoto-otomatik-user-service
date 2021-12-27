package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.account.model.UpdatePasswordRequest;
import com.demo.keycloak.client.KeycloakTokenClient;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.exception.AuthenticationException;
import com.demo.keycloak.model.KeycloakResetPasswordRequest;
import com.demo.keycloak.model.KeycloakUser;
import com.demo.token.service.TokenDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.demo.ErrorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UpdatePasswordServiceTest {
    private static final String PASSWORD_RESET_CODE = "passwordResetCode";
    private static final String USERNAME = "nikkinicholas.romero@gmail.com";
    private static final String TOKEN = "some_token";
    private static final String ID = "some_id";
    private static final String PASSWORD = "password";

    @InjectMocks
    private UpdatePasswordService target;

    @Mock
    private TokenDecoder tokenDecoder;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Mock
    private KeycloakTokenClient keycloakTokenClient;

    private UpdatePasswordRequest request;

    @Captor
    private ArgumentCaptor<KeycloakResetPasswordRequest> argumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "passwordResetCode", PASSWORD_RESET_CODE);

        request = new UpdatePasswordRequest();
        request.setOldPassword("old_password");
        request.setNewPassword("new_password");

        when(tokenDecoder.getSubject(anyString())).thenReturn(USERNAME);
    }

    @Test
    public void updatePassword_whenUserDoesNotExist() {
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.empty());

        RequestException e = assertThrows(RequestException.class, () -> target.updatePassword(TOKEN, request));
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);

        verify(tokenDecoder).getSubject(TOKEN);
        verifyNoInteractions(keycloakTokenClient);
        verify(keycloakUserClient, never()).updateUser(anyString(), any());
    }

    @Test
    public void updatePassword_whenUserIsNotYetActivated() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setEmailVerified(false);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () -> target.updatePassword(TOKEN, request));
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);

        verify(tokenDecoder).getSubject(TOKEN);
        verifyNoInteractions(keycloakTokenClient);
        verify(keycloakUserClient, never()).updateUser(anyString(), any());
    }

    @Test
    public void updatePassword_whenOldPasswordIsIncorrect() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setEmailVerified(true);
        keycloakUser.setUsername(USERNAME);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));
        when(keycloakTokenClient.getKeycloakToken(anyString(), anyString())).thenReturn("");

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> target.updatePassword(TOKEN, request));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_TOKEN_ERROR_MESSAGE);

        verify(tokenDecoder).getSubject(TOKEN);
        verify(keycloakTokenClient).getKeycloakToken(USERNAME, request.getOldPassword());
        verify(keycloakUserClient, never()).updateUser(anyString(), any());
    }

    @Test
    public void updatePassword() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setEmailVerified(true);
        keycloakUser.setUsername(USERNAME);
        keycloakUser.setId(ID);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));
        when(keycloakTokenClient.getKeycloakToken(anyString(), anyString())).thenReturn(TOKEN);

        target.updatePassword(TOKEN, request);

        verify(tokenDecoder).getSubject(TOKEN);
        verify(keycloakTokenClient).getKeycloakToken(USERNAME, request.getOldPassword());
        verify(keycloakUserClient).updateUser(eq(ID), argumentCaptor.capture());
        KeycloakResetPasswordRequest actual = argumentCaptor.getValue();
        assertThat(actual).isNotNull();
        assertThat(actual.getAttributes()).isNotEmpty();
        assertThat(actual.getAttributes().containsKey(PASSWORD_RESET_CODE)).isTrue();
        assertThat(actual.getAttributes().get(PASSWORD_RESET_CODE)).isEmpty();
        assertThat(actual.getCredentials()).isNotEmpty();
        assertThat(actual.getCredentials().size()).isEqualTo(1);
        assertThat(actual.getCredentials().get(0)).isNotNull();
        assertThat(actual.getCredentials().get(0).getType()).isEqualTo(PASSWORD);
        assertThat(actual.getCredentials().get(0).getValue()).isEqualTo(request.getNewPassword());
        assertThat(actual.getCredentials().get(0).isTemporary()).isFalse();
    }
}
