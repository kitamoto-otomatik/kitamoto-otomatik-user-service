package com.demo.service;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TokenDecodeServiceTest {
    private static final String SECRET_KEY = "some_secret_key";
    private static final String TOKEN = "some_token";

    @InjectMocks
    private TokenDecodeService target;

    @Mock
    private JwtService jwtService;

    @Mock
    private Claims claims;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "secretKey", SECRET_KEY);
    }

    @Test
    public void decode_whenJwtServiceThrowsException() {
        doThrow(new RuntimeException()).when(jwtService).decodeJWT(anyString(), anyString());

        Optional<Claims> actual = target.decode(TOKEN);

        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isFalse();

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }

    @Test
    public void decode_whenClaimsIsExpired() {
        when(jwtService.decodeJWT(anyString(), anyString()))
                .thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(LocalDateTime.now().minusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()));

        Optional<Claims> actual = target.decode(TOKEN);

        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isFalse();

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }

    @Test
    public void decode_whenClaimsIsValid() {
        when(jwtService.decodeJWT(anyString(), anyString()))
                .thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(LocalDateTime.now().plusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()));

        Optional<Claims> actual = target.decode(TOKEN);

        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(claims);

        verify(jwtService).decodeJWT(SECRET_KEY, TOKEN);
    }
}
