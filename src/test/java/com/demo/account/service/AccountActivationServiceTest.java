package com.demo.account.service;

import com.demo.account.exception.RequestException;
import com.demo.keycloak.client.KeycloakUserClient;
import com.demo.keycloak.model.AccountActivationRequest;
import com.demo.keycloak.model.KeycloakUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.demo.ErrorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AccountActivationServiceTest {
    private static final String ACCOUNT_ACTIVATION_CODE = "accountActivationCode";

    @InjectMocks
    private AccountActivationService target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Captor
    private ArgumentCaptor<AccountActivationRequest> argumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "accountActivationCode", ACCOUNT_ACTIVATION_CODE);
    }

    @Test
    public void activateAccount_whenUsernameDoesNotExist() {
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.empty());

        RequestException e = assertThrows(RequestException.class, () ->
                target.activateAccount("non-existent@gmail.com", "123456"));
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("non-existent@gmail.com");
        verify(keycloakUserClient, never()).updateUser(any(), any());
    }

    @Test
    public void activateAccount_whenUserIsAlreadyActivated() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("123456"));

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(attributes);
        keycloakUser.setEmailVerified(true);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () ->
                target.activateAccount("nikkinicholas.romero@gmail.com", "123456"));
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateUser(any(), any());
    }

    @Test
    public void activateAccount_whenAttributesIsEmpty() {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(new HashMap<>());
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () ->
                target.activateAccount("nikkinicholas.romero@gmail.com", "123456"));
        assertThat(e.getMessage()).isEqualTo(ACTIVATION_CODE_IS_INCORRECT_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateUser(any(), any());
    }

    @Test
    public void activateAccount_whenDoesNotContainAccountActivationCodeAttribute() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("someOtherAttribute", Collections.singletonList("123456"));

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(attributes);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () ->
                target.activateAccount("nikkinicholas.romero@gmail.com", "123456"));
        assertThat(e.getMessage()).isEqualTo(ACTIVATION_CODE_IS_INCORRECT_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateUser(any(), any());
    }

    @Test
    public void activateAccount_whenAccountActivationCodeAttributesIsEmpty() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, new ArrayList<>());

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(attributes);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () ->
                target.activateAccount("nikkinicholas.romero@gmail.com", "123456"));
        assertThat(e.getMessage()).isEqualTo(ACTIVATION_CODE_IS_INCORRECT_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateUser(any(), any());
    }

    @Test
    public void activateAccount_whenAccountActivationCodeIsEmpty() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList(""));

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(attributes);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () ->
                target.activateAccount("nikkinicholas.romero@gmail.com", "123456"));
        assertThat(e.getMessage()).isEqualTo(ACTIVATION_CODE_IS_INCORRECT_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateUser(any(), any());
    }

    @Test
    public void activateAccount_whenAccountActivationCodeIsIncorrect() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("123456"));

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(attributes);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        RequestException e = assertThrows(RequestException.class, () ->
                target.activateAccount("nikkinicholas.romero@gmail.com", "111111"));
        assertThat(e.getMessage()).isEqualTo(ACTIVATION_CODE_IS_INCORRECT_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateUser(any(), any());
    }

    @Test
    public void activateAccount() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACCOUNT_ACTIVATION_CODE, Collections.singletonList("123456"));

        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId("someId");
        keycloakUser.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser.setAttributes(attributes);
        when(keycloakUserClient.getUserByUsername(anyString())).thenReturn(Optional.of(keycloakUser));

        target.activateAccount("nikkinicholas.romero@gmail.com", "123456");

        verify(keycloakUserClient).getUserByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient).updateUser(eq("someId"), argumentCaptor.capture());
        AccountActivationRequest accountActivationRequest = argumentCaptor.getValue();
        assertThat(accountActivationRequest).isNotNull();
        assertThat(accountActivationRequest.isEmailVerified()).isTrue();
        assertThat(accountActivationRequest.isEnabled()).isTrue();
    }
}
