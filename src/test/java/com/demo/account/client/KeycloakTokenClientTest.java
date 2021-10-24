package com.demo.account.client;

import com.demo.account.exception.KeycloakException;
import com.demo.account.model.AccessToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class KeycloakTokenClientTest {
    private MockWebServer server;
    private ObjectMapper mapper;

    @InjectMocks
    private KeycloakTokenClient target;

    @BeforeEach
    public void setup() throws IOException {
        server = new MockWebServer();
        server.start();

        mapper = new ObjectMapper();

        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "host", server.url("").toString());
        ReflectionTestUtils.setField(target, "tokenEndpoint", "/auth/realms/kitamoto-otomatik/protocol/openid-connect/token");
        ReflectionTestUtils.setField(target, "grantTypeKey", "grant_type");
        ReflectionTestUtils.setField(target, "grantTypeValue", "client_credentials");
        ReflectionTestUtils.setField(target, "clientIdKey", "client_id");
        ReflectionTestUtils.setField(target, "clientIdValue", "admin-cli");
        ReflectionTestUtils.setField(target, "clientSecretKey", "client_secret");
        ReflectionTestUtils.setField(target, "clientSecretValue", "81aafc7d-275c-4a08-806e-37a056442173");

        target.postConstruct();
    }

    @Test
    public void getKeycloakToken_whenOk() throws InterruptedException, JsonProcessingException {
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("someAccessToken");

        MockResponse response = new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(mapper.writeValueAsString(accessToken))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        server.enqueue(response);

        StepVerifier.create(target.getKeycloakToken())
                .expectNext("someAccessToken")
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/auth/realms/kitamoto-otomatik/protocol/openid-connect/token");
        assertThat(request.getHeader(HttpHeaders.CONTENT_TYPE)).startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        assertThat(request.getBody().readUtf8()).isEqualTo("grant_type=client_credentials&client_id=admin-cli&client_secret=81aafc7d-275c-4a08-806e-37a056442173");
    }

    @Test
    public void getKeycloakToken_whenError() throws InterruptedException {
        MockResponse response = new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        server.enqueue(response);

        StepVerifier.create(target.getKeycloakToken())
                .expectError(KeycloakException.class)
                .verify();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/auth/realms/kitamoto-otomatik/protocol/openid-connect/token");
        assertThat(request.getHeader(HttpHeaders.CONTENT_TYPE)).startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        assertThat(request.getBody().readUtf8()).isEqualTo("grant_type=client_credentials&client_id=admin-cli&client_secret=81aafc7d-275c-4a08-806e-37a056442173");
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }
}
