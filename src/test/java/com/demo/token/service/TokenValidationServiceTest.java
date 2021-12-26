package com.demo.token.service;

import com.demo.keycloak.exception.AuthenticationException;
import com.demo.token.model.ValidateTokenRequest;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.demo.ErrorMessage.KEYCLOAK_TOKEN_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TokenValidationServiceTest {
    private static final String SECRET_KEY = "some_secret_key";
    private static final String TOKEN = "some_token";

    @InjectMocks
    private TokenValidationService target;

    @Mock
    private JwtService jwtService;

    @Mock
    private Claims claims;

    private ValidateTokenRequest request;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "secretKey", SECRET_KEY);

        request = new ValidateTokenRequest();
        request.setToken(TOKEN);
    }

    @Test
    public void validate() {
        when(jwtService.decodeJWT(anyString(), anyString()))
                .thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(LocalDateTime.now().plusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()));

        target.validate(request);

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }

    @Test
    public void validate_whenExpired() {
        when(jwtService.decodeJWT(anyString(), anyString()))
                .thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(LocalDateTime.now().minusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()));

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> target.validate(request));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_TOKEN_ERROR_MESSAGE);

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }

    @Test
    public void validate_whenError() {
        doThrow(new RuntimeException()).when(jwtService).decodeJWT(anyString(), anyString());

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> target.validate(request));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_TOKEN_ERROR_MESSAGE);

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }
}
