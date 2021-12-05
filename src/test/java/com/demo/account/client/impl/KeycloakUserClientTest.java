package com.demo.account.client.impl;

import com.demo.account.client.KeycloakTokenClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.model.Credential;
import com.demo.account.model.KeycloakErrorResponse;
import com.demo.account.model.KeycloakUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KeycloakUserClientTest {
    private static final String HOST = "http://localhost";
    private static final String USERS_ENDPOINT = "/users";

    private MockWebServer mockBackEnd;

    private ObjectMapper objectMapper;

    @InjectMocks
    private KeycloakUserClientImpl target;

    @Mock
    private KeycloakTokenClient tokenService;

    private KeycloakUser keycloakUser;

    @BeforeEach
    public void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        objectMapper = new ObjectMapper();

        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "host", HOST + ":" + mockBackEnd.getPort());
        ReflectionTestUtils.setField(target, "usersEndpoint", USERS_ENDPOINT);

        when(tokenService.getKeycloakToken()).thenReturn("someAccessToken");

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("verificationCode", Collections.singletonList("123"));

        Credential credential = new Credential();
        credential.setType("password");
        credential.setValue("somePassword");
        credential.setTemporary(false);

        keycloakUser = new KeycloakUser();
        keycloakUser.setUsername("someUsername");
        keycloakUser.setEmail("someEmail");
        keycloakUser.setFirstName("someFirstName");
        keycloakUser.setLastName("someLastName");
        keycloakUser.setAttributes(attributes);
        keycloakUser.setCredentials(Collections.singletonList(credential));
        keycloakUser.setEmailVerified(true);
        keycloakUser.setEnabled(true);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void getUserListByUsername_whenOk() throws JsonProcessingException, InterruptedException {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(keycloakUserList))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<KeycloakUser> actual = target.getUserListByUsername("nikkinicholas.romero@gmail.com");
        assertThat(actual).isEqualTo(keycloakUserList);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo(USERS_ENDPOINT + "?username=nikkinicholas.romero@gmail.com");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void getUserListByUsername_whenError() throws InterruptedException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        KeycloakException e = assertThrows(KeycloakException.class, () -> target.getUserListByUsername("nikkinicholas.romero@gmail.com"));
        assertThat(e.getMessage()).isEqualTo("Could not get Keycloak users");

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo(USERS_ENDPOINT + "?username=nikkinicholas.romero@gmail.com");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void createAccount_whenOk() throws InterruptedException, JsonProcessingException {
        mockBackEnd.enqueue(new MockResponse());

        target.createAccount(keycloakUser);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo(USERS_ENDPOINT);
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), KeycloakUser.class)).isEqualTo(keycloakUser);

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void createAccount_whenError() throws InterruptedException, JsonProcessingException {
        KeycloakErrorResponse keycloakErrorResponse = new KeycloakErrorResponse();
        keycloakErrorResponse.setErrorMessage("User already exists");

        MockResponse mockResponse = new MockResponse()
                .setBody(objectMapper.writeValueAsString(keycloakErrorResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        KeycloakException e = assertThrows(KeycloakException.class, () -> target.createAccount(keycloakUser));
        assertThat(e.getMessage()).isEqualTo("User already exists");

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo(USERS_ENDPOINT);
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), KeycloakUser.class)).isEqualTo(keycloakUser);

        verify(tokenService).getKeycloakToken();
    }
}
