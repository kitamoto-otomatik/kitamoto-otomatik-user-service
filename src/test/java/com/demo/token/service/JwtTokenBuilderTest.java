package com.demo.token.service;

import com.demo.token.model.Token;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JwtTokenBuilderTest {
    private static final String SECRET_KEY = "some_secret_key";
    private static final String ISSUER = "some_issuer";
    private static final long TOKEN_EXPIRE = 3_600;
    private static final String ACCESS_TOKEN = "some_token";
    private static final String SUBJECT = "some_subject";

    @InjectMocks
    private JwtTokenBuilder target;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(target, "issuer", ISSUER);
        ReflectionTestUtils.setField(target, "tokenExpire", TOKEN_EXPIRE);

        when(jwtService.createJWT(eq(SECRET_KEY), anyString(), eq(ISSUER), eq(SUBJECT), eq(TOKEN_EXPIRE)))
                .thenReturn(ACCESS_TOKEN);
    }

    @Test
    public void buildToken() throws InterruptedException {
        Token actual = target.buildToken(SUBJECT);

        Thread.sleep(1000);

        assertThat(actual).isNotNull();
        assertThat(actual).isNotNull();
        assertThat(actual.getToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(actual.getExpire()).isBefore(LocalDateTime.now().plusSeconds(TOKEN_EXPIRE));

        verify(jwtService).createJWT(eq(SECRET_KEY), anyString(), eq(ISSUER), eq(SUBJECT), eq(TOKEN_EXPIRE));
    }
}
