package com.demo.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.model.CreateAccountRequest;
import com.demo.account.model.KeycloakUser;
import com.demo.account.service.CreateAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class CreateAccountServiceTest {
    @InjectMocks
    private CreateAccountService target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Captor
    private ArgumentCaptor<KeycloakUser> argumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createAccount() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("someUsername");
        request.setPassword("somePassword");
        request.setFirstName("someFirstname");
        request.setLastName("someLastname");

        target.createAccount(request);

        verify(keycloakUserClient).createAccount(argumentCaptor.capture());
        KeycloakUser keycloakUser = argumentCaptor.getValue();
        assertThat(keycloakUser).isNotNull();
        assertThat(keycloakUser.getUsername()).isEqualTo("someUsername");
        assertThat(keycloakUser.getEmail()).isEqualTo("someUsername");
        assertThat(keycloakUser.getFirstName()).isEqualTo("someFirstname");
        assertThat(keycloakUser.getLastName()).isEqualTo("someLastname");
        assertThat(keycloakUser.getAttributes()).isNotEmpty();
        assertThat(keycloakUser.getAttributes().size()).isEqualTo(1);
        assertThat(keycloakUser.getAttributes().containsKey("verificationCode")).isTrue();
        assertThat(keycloakUser.getAttributes().get("verificationCode")).isNotEmpty();
        assertThat(keycloakUser.getAttributes().get("verificationCode").get(0)).isNotBlank();
        assertThat(keycloakUser.getCredentials()).isNotEmpty();
        assertThat(keycloakUser.getCredentials().size()).isEqualTo(1);
        assertThat(keycloakUser.getCredentials().get(0)).isNotNull();
        assertThat(keycloakUser.getCredentials().get(0).getType()).isEqualTo("password");
        assertThat(keycloakUser.getCredentials().get(0).getValue()).isEqualTo("somePassword");
        assertThat(keycloakUser.getCredentials().get(0).isTemporary()).isFalse();
        assertThat(keycloakUser.isEmailVerified()).isFalse();
        assertThat(keycloakUser.isEnabled()).isFalse();
    }
}
