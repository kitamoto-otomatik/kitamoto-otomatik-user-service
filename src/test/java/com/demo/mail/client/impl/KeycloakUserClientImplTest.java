package com.demo.mail.client.impl;

import com.demo.keycloak.client.KeycloakTokenClient;
import com.demo.keycloak.client.impl.KeycloakUserClientImpl;
import com.demo.keycloak.exception.KeycloakException;
import com.demo.keycloak.model.*;
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

import static com.demo.ErrorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KeycloakUserClientImplTest {
    private static final String HOST = "http://localhost";
    private static final String ENDPOINT = "/users";

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
        ReflectionTestUtils.setField(target, "endpoint", ENDPOINT);

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
    public void getUserByUsername() throws JsonProcessingException, InterruptedException {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("nikkinicholas.romero@gmail.coms");
        keycloakUser2.setEmailVerified(true);
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(keycloakUserList))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Optional<KeycloakUser> actual = target.getUserByUsername("nikkinicholas.romero@gmail.com");
        assertThat(actual).isEqualTo(Optional.of(keycloakUser1));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT + "?username=nikkinicholas.romero@gmail.com");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void getUserListByUsername_whenError() throws InterruptedException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        KeycloakException e = assertThrows(KeycloakException.class, () -> target.getUserByUsername("nikkinicholas.romero@gmail.com"));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_GET_USER_ERROR_MESSAGE);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT + "?username=nikkinicholas.romero@gmail.com");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void createAccount_whenOk() throws InterruptedException, JsonProcessingException {
        mockBackEnd.enqueue(new MockResponse());

        target.createAccount(keycloakUser);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT);
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
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_USER_CREATION_ERROR_MESSAGE);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT);
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), KeycloakUser.class)).isEqualTo(keycloakUser);

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void activateAccount_whenOk() throws InterruptedException, JsonProcessingException {
        mockBackEnd.enqueue(new MockResponse());
        AccountActivationRequest accountActivationRequest = new AccountActivationRequest();
        accountActivationRequest.setEmailVerified(true);
        accountActivationRequest.setEnabled(true);
        target.updateUser("someId", accountActivationRequest);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT + "/someId");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), AccountActivationRequest.class)).isEqualTo(accountActivationRequest);

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void activateAccount_whenError() throws InterruptedException, JsonProcessingException {
        KeycloakErrorResponse keycloakErrorResponse = new KeycloakErrorResponse();
        keycloakErrorResponse.setErrorMessage("User does not exist");

        MockResponse mockResponse = new MockResponse()
                .setBody(objectMapper.writeValueAsString(keycloakErrorResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        AccountActivationRequest accountActivationRequest = new AccountActivationRequest();
        accountActivationRequest.setEmailVerified(true);
        accountActivationRequest.setEnabled(true);
        KeycloakException e = assertThrows(KeycloakException.class, () -> target.updateUser("someId", accountActivationRequest));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_USER_UPDATE_ERROR_MESSAGE);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT + "/someId");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), AccountActivationRequest.class)).isEqualTo(accountActivationRequest);

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void updateKeycloakAccountAttribute_whenOk() throws InterruptedException, JsonProcessingException {
        mockBackEnd.enqueue(new MockResponse());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("activationCode", Collections.singletonList("1234"));
        KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest = new KeycloakAccountAttributeUpdateRequest();
        keycloakAccountAttributeUpdateRequest.setAttributes(attributes);
        target.updateUser("someId", keycloakAccountAttributeUpdateRequest);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT + "/someId");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), KeycloakAccountAttributeUpdateRequest.class)).isEqualTo(keycloakAccountAttributeUpdateRequest);

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void updateKeycloakAccountAttribute_whenError() throws InterruptedException, JsonProcessingException {
        KeycloakErrorResponse keycloakErrorResponse = new KeycloakErrorResponse();
        keycloakErrorResponse.setErrorMessage("User does not exist");

        MockResponse mockResponse = new MockResponse()
                .setBody(objectMapper.writeValueAsString(keycloakErrorResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("activationCode", Collections.singletonList("1234"));
        KeycloakAccountAttributeUpdateRequest keycloakAccountAttributeUpdateRequest = new KeycloakAccountAttributeUpdateRequest();
        keycloakAccountAttributeUpdateRequest.setAttributes(attributes);
        KeycloakException e = assertThrows(KeycloakException.class, () -> target.updateUser("someId", keycloakAccountAttributeUpdateRequest));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_USER_UPDATE_ERROR_MESSAGE);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT + "/someId");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), KeycloakAccountAttributeUpdateRequest.class)).isEqualTo(keycloakAccountAttributeUpdateRequest);

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void updateKeycloakAccountCredentials_whenOk() throws InterruptedException, JsonProcessingException {
        mockBackEnd.enqueue(new MockResponse());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("passwordResetCode", new ArrayList<>());
        Credential credential = new Credential();
        credential.setType("password");
        credential.setValue("somePassword");
        credential.setTemporary(false);
        KeycloakResetPasswordRequest keycloakResetPasswordRequest = new KeycloakResetPasswordRequest();
        keycloakResetPasswordRequest.setAttributes(attributes);
        keycloakResetPasswordRequest.setCredentials(Collections.singletonList(credential));
        target.updateUser("someId", keycloakResetPasswordRequest);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT + "/someId");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), KeycloakResetPasswordRequest.class)).isEqualTo(keycloakResetPasswordRequest);

        verify(tokenService).getKeycloakToken();
    }

    @Test
    public void updateKeycloakAccountCredentials_whenError() throws InterruptedException, JsonProcessingException {
        KeycloakErrorResponse keycloakErrorResponse = new KeycloakErrorResponse();
        keycloakErrorResponse.setErrorMessage("User does not exist");

        MockResponse mockResponse = new MockResponse()
                .setBody(objectMapper.writeValueAsString(keycloakErrorResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        mockResponse.status("HTTP/1.1 500 Internal Server Error");
        mockBackEnd.enqueue(mockResponse);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("passwordResetCode", new ArrayList<>());
        Credential credential = new Credential();
        credential.setType("password");
        credential.setValue("somePassword");
        credential.setTemporary(false);
        KeycloakResetPasswordRequest keycloakResetPasswordRequest = new KeycloakResetPasswordRequest();
        keycloakResetPasswordRequest.setAttributes(attributes);
        keycloakResetPasswordRequest.setCredentials(Collections.singletonList(credential));
        KeycloakException e = assertThrows(KeycloakException.class, () -> target.updateUser("someId", keycloakResetPasswordRequest));
        assertThat(e.getMessage()).isEqualTo(KEYCLOAK_USER_UPDATE_ERROR_MESSAGE);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("PUT");
        assertThat(recordedRequest.getPath()).isEqualTo(ENDPOINT + "/someId");
        assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(objectMapper.readValue(recordedRequest.getBody().readUtf8(), KeycloakResetPasswordRequest.class)).isEqualTo(keycloakResetPasswordRequest);

        verify(tokenService).getKeycloakToken();
    }
}
