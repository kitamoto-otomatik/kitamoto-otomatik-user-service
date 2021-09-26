package com.demo.service;

import com.demo.model.AuthenticationResponse;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TokenRefreshServiceTest {
    private static final String REFRESH_TOKEN = "some_refresh_token";
    private static final String SUBJECT = "some_subject";

    @InjectMocks
    private TokenRefreshService target;

    @Mock
    private TokenDecodeService tokenDecodeService;

    @Mock
    private AuthenticationResponseBuilder authenticationResponseBuilder;

    @Mock
    private Claims claims;

    @Mock
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void refresh_whenInvalidRefreshToken() {
        when(tokenDecodeService.decode(anyString())).thenReturn(Optional.empty());

        Optional<AuthenticationResponse> actual = target.refresh(REFRESH_TOKEN);

        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isFalse();

        verify(tokenDecodeService).decode(REFRESH_TOKEN);
        verifyNoInteractions(authenticationResponseBuilder);
    }

    @Test
    public void refresh_whenValidRefreshToken() {
        when(tokenDecodeService.decode(anyString())).thenReturn(Optional.of(claims));
        when(authenticationResponseBuilder.buildAuthenticationResponse(anyString())).thenReturn(authenticationResponse);
        when(claims.getSubject()).thenReturn(SUBJECT);


        Optional<AuthenticationResponse> actual = target.refresh(REFRESH_TOKEN);

        assertThat(actual).isNotNull();
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(authenticationResponse);

        verify(tokenDecodeService).decode(REFRESH_TOKEN);
        verify(authenticationResponseBuilder).buildAuthenticationResponse(SUBJECT);
    }
}
