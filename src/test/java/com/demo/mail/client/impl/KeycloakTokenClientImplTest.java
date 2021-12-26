package com.demo.mail.client.impl;

import com.demo.keycloak.client.impl.KeycloakTokenClientImpl;
import com.demo.keycloak.exception.KeycloakException;
import com.demo.keycloak.model.AccessToken;
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
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static com.demo.ErrorMessage.KEYCLOAK_TOKEN_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeycloakTokenClientImplTest {
    private static final String HOST = "http://localhost";
    private static final String ENDPOINT = "/token";
    private static final String GRANT_TYPE_KEY = "someGrantType";
    private static final String GRANT_TYPE_VALUE = "someGrantTypeValue";
    private static final String CLIENT_ID_KEY = "someClientIdKey";
    private static final String CLIENT_ID_VALUE = "someClientIdKeyValue";
    private static final String CLIENT_SECRET_KEY = "someClientSecretKey";
    private static final String CLIENT_SECRET_VALUE = "someClientSecretKeyValue";

    private MockWebServer mockBackEnd;

    private ObjectMapper objectMapper;

    @InjectMocks
    private KeycloakTokenClientImpl target;

    @BeforeEach
    public void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        objectMapper = new ObjectMapper();

        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "host", HOST + ":" + mockBackEnd.getPort());
        ReflectionTestUtils.setField(target, "endpoint", ENDPOINT);
        ReflectionTestUtils.setField(target, "grantTypeKey", GRANT_TYPE_KEY);
        ReflectionTestUtils.setField(target, "grantTypeValue", GRANT_TYPE_VALUE);
        ReflectionTestUtils.setField(target, "clientIdKey", CLIENT_ID_KEY);
        ReflectionTestUtils.setField(target, "clientIdValue", CLIENT_ID_VALUE);
        ReflectionTestUtils.setField(target, "clientSecretKey", CLIENT_SECRET_KEY);
        ReflectionTestUtils.setField(target, "clientSecretValue", CLIENT_SECRET_VALUE);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void getKeycloakToken_whenOk() throws JsonProcessingException, InterruptedException {
        AccessToken accessToken = new AccessToken("someAccessToken");
        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(accessToken))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        assertThat(target.getKeycloakToken()).isEqualTo("someAccessToken");

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT);
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("someGrantType=someGrantTypeValue&someClientIdKey=someClientIdKeyValue&someClientSecretKey=someClientSecretKeyValue");
    }

    @Test
    public void getKeycloakToken_whenError() throws InterruptedException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        KeycloakException e = assertThrows(KeycloakException.class, () -> target.getKeycloakToken());
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_TOKEN_ERROR_MESSAGE);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT);
        assertThat(recordedRequest.getBody().readUtf8()).isEqualTo("someGrantType=someGrantTypeValue&someClientIdKey=someClientIdKeyValue&someClientSecretKey=someClientSecretKeyValue");
    }
}
