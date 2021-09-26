package com.demo.service;

import com.demo.model.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AuthenticationResponseBuilderTest {
    private static final String SECRET_KEY = "some_secret_key";
    private static final String ISSUER = "some_issuer";
    private static final long ACCESS_TOKEN_EXPIRE = 3_600;
    private static final long REFRESH_TOKEN_EXPIRE = 36_000;
    private static final String ACCESS_TOKEN = "some_access_token";
    private static final String REFRESH_TOKEN = "some_refresh_token";
    private static final String SUBJECT = "some_subject";

    @InjectMocks
    private AuthenticationResponseBuilder target;

    @Mock
    private JwtService jwtService;

    @Mock
    private UuidGenerator uuidGenerator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(target, "issuer", ISSUER);
        ReflectionTestUtils.setField(target, "accessTokenExpire", ACCESS_TOKEN_EXPIRE);
        ReflectionTestUtils.setField(target, "refreshTokenExpire", REFRESH_TOKEN_EXPIRE);

        when(jwtService.createJWT(eq(SECRET_KEY), anyString(), eq(ISSUER), eq(SUBJECT), eq(ACCESS_TOKEN_EXPIRE)))
                .thenReturn(ACCESS_TOKEN);
        when(jwtService.createJWT(eq(SECRET_KEY), anyString(), eq(ISSUER), eq(SUBJECT), eq(REFRESH_TOKEN_EXPIRE)))
                .thenReturn(REFRESH_TOKEN);

        when(uuidGenerator.generateRandomUuid()).thenCallRealMethod();
    }

    @Test
    public void buildAuthenticationResponse() {
        AuthenticationResponse actual = target.buildAuthenticationResponse(SUBJECT);

        assertThat(actual).isNotNull();
        assertThat(actual.getAccessToken()).isNotNull();
        assertThat(actual.getAccessToken().getToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(actual.getAccessToken().getExpire()).isBefore(LocalDateTime.now().plusSeconds(ACCESS_TOKEN_EXPIRE));
        assertThat(actual.getRefreshToken()).isNotNull();
        assertThat(actual.getRefreshToken().getToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(actual.getRefreshToken().getExpire()).isBefore(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRE));

        assertThat(actual.getAccessToken().getExpire()).isBefore(actual.getRefreshToken().getExpire());
    }
}
