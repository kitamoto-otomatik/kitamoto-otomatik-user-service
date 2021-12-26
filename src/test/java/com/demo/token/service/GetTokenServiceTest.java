package com.demo.token.service;

import com.demo.keycloak.client.KeycloakTokenClient;
import com.demo.keycloak.exception.AuthenticationException;
import com.demo.token.model.GetTokenRequest;
import com.demo.token.model.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.demo.ErrorMessage.KEYCLOAK_TOKEN_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GetTokenServiceTest {
    @InjectMocks
    private GetTokenService target;

    @Mock
    private KeycloakTokenClient keycloakTokenClient;

    @Mock
    private JwtTokenBuilder jwtTokenBuilder;

    private GetTokenRequest request;

    @Mock
    private Token token;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        request = new GetTokenRequest();
        request.setUsername("someUsername");
        request.setPassword("somePassword");
    }

    @Test
    public void getToken() {
        when(keycloakTokenClient.getKeycloakToken(anyString(), anyString()))
                .thenReturn("someToken");
        when(jwtTokenBuilder.buildToken(request.getUsername()))
                .thenReturn(token);
        Token actual = target.getToken(request);

        assertThat(actual).isEqualTo(token);

        verify(keycloakTokenClient).getKeycloakToken(request.getUsername(), request.getPassword());
        verify(jwtTokenBuilder).buildToken(request.getUsername());
    }

    @Test
    public void getToken_whenError() {
        when(keycloakTokenClient.getKeycloakToken(anyString(), anyString()))
                .thenReturn("");
        when(jwtTokenBuilder.buildToken(request.getUsername()))
                .thenReturn(token);
        AuthenticationException e = assertThrows(AuthenticationException.class, () ->
                target.getToken(request));

        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_TOKEN_ERROR_MESSAGE);

        verify(keycloakTokenClient).getKeycloakToken(request.getUsername(), request.getPassword());
        verifyNoInteractions(jwtTokenBuilder);
    }
}
