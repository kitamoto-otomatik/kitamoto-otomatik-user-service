package com.demo.account.client;

import com.demo.account.exception.KeycloakException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class KeycloakUserClientTest {
    private MockWebServer server;
    private ObjectMapper mapper;

    @InjectMocks
    private KeycloakUserClient target;

    @Mock
    private KeycloakTokenClient tokenService;

    @BeforeEach
    public void setup() throws IOException {
        server = new MockWebServer();
        server.start();

        mapper = new ObjectMapper();

        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "host", server.url("").toString());
        ReflectionTestUtils.setField(target, "usersEndpoint", "/auth/admin/realms/kitamoto-otomatik/users");

        target.postConstruct();
    }

    @Test
    public void getUserListByUsername_whenOk() throws JsonProcessingException, InterruptedException {
        when(tokenService.getKeycloakToken()).thenReturn(Mono.just("someAccessToken"));

        KeycloakUser keycloakUser = new KeycloakUser();
        KeycloakUser[] keycloakUserArray = new KeycloakUser[]{keycloakUser};

        MockResponse response = new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(mapper.writeValueAsString(keycloakUserArray))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        server.enqueue(response);

        StepVerifier.create(target.getUserListByUsername("nikkinicholas.romero@gmail.com"))
                .expectNext(Arrays.asList(keycloakUserArray))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/auth/admin/realms/kitamoto-otomatik/users?username=nikkinicholas.romero@gmail.com");
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
    }

    @Test
    public void getUserListByUsername_whenError() throws InterruptedException {
        when(tokenService.getKeycloakToken()).thenReturn(Mono.just("someAccessToken"));

        MockResponse response = new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        server.enqueue(response);

        StepVerifier.create(target.getUserListByUsername("nikkinicholas.romero@gmail.com"))
                .expectError(KeycloakException.class)
                .verify();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/auth/admin/realms/kitamoto-otomatik/users?username=nikkinicholas.romero@gmail.com");
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
    }

    @Test
    public void getUserListByUsername_whenTokenServiceErrors() {
        when(tokenService.getKeycloakToken()).thenReturn(Mono.error(new KeycloakException("")));

        StepVerifier.create(target.getUserListByUsername("nikkinicholas.romero@gmail.com"))
                .expectError(KeycloakException.class)
                .verify();
    }

    @Test
    public void createAccount_whenOk() throws JsonProcessingException, InterruptedException {
        when(tokenService.getKeycloakToken()).thenReturn(Mono.just("someAccessToken"));

        MockResponse response = new MockResponse()
                .setResponseCode(HttpStatus.CREATED.value());

        server.enqueue(response);

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setEmail("nikkinicholas.romero@gmail.com");
        keycloakUser.setFirstName("Nikki Nicholas");
        keycloakUser.setLastName("Romero");

        StepVerifier.create(target.createAccount(keycloakUser))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/auth/admin/realms/kitamoto-otomatik/users");
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(request.getBody().readUtf8()).isEqualTo(mapper.writeValueAsString(keycloakUser));
    }

    @Test
    public void createAccount_whenError() throws InterruptedException, JsonProcessingException {
        when(tokenService.getKeycloakToken()).thenReturn(Mono.just("someAccessToken"));

        MockResponse response = new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        server.enqueue(response);

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setEmail("nikkinicholas.romero@gmail.com");
        keycloakUser.setFirstName("Nikki Nicholas");
        keycloakUser.setLastName("Romero");

        StepVerifier.create(target.createAccount(keycloakUser))
                .expectError(KeycloakException.class)
                .verify();

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/auth/admin/realms/kitamoto-otomatik/users");
        assertThat(request.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer someAccessToken");
        assertThat(request.getBody().readUtf8()).isEqualTo(mapper.writeValueAsString(keycloakUser));
    }

    @Test
    public void createAccount_whenTokenServiceErrors() {
        when(tokenService.getKeycloakToken()).thenReturn(Mono.error(new KeycloakException("")));

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setEmail("nikkinicholas.romero@gmail.com");
        keycloakUser.setFirstName("Nikki Nicholas");
        keycloakUser.setLastName("Romero");

        StepVerifier.create(target.createAccount(keycloakUser))
                .expectError(KeycloakException.class)
                .verify();
    }

    @AfterEach
    public void tearDown() throws IOException {
        server.shutdown();
    }
}
