package com.demo.token.service;

import com.demo.keycloak.exception.AuthenticationException;
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

public class TokenDecoderTest {
    private static final String SECRET_KEY = "some_secret_key";
    private static final String TOKEN = "some_token";
    private static final String SUBJECT = "some_subject";

    @InjectMocks
    private TokenDecoder target;

    @Mock
    private JwtService jwtService;

    @Mock
    private Claims claims;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "secretKey", SECRET_KEY);

        when(claims.getSubject()).thenReturn(SUBJECT);
    }

    @Test
    public void getSubject() {
        when(jwtService.decodeJWT(anyString(), anyString()))
                .thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(LocalDateTime.now().plusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()));

        assertThat(target.getSubject(TOKEN)).isEqualTo(SUBJECT);

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }

    @Test
    public void getSubject_whenExpired() {
        when(jwtService.decodeJWT(anyString(), anyString()))
                .thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(LocalDateTime.now().minusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()));

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> target.getSubject(TOKEN));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_TOKEN_ERROR_MESSAGE);

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }

    @Test
    public void getSubject_whenError() {
        doThrow(new RuntimeException()).when(jwtService).decodeJWT(anyString(), anyString());

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> target.getSubject(TOKEN));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_TOKEN_ERROR_MESSAGE);

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }
}
