package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.AccountActivationRequest;
import com.demo.account.model.KeycloakUser;
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
    private static final String ACTIVATION_CODE = "activationCode";

    @InjectMocks
    private AccountActivationService target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    @Captor
    private ArgumentCaptor<AccountActivationRequest> argumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "code", ACTIVATION_CODE);
    }

    @Test
    public void activateAccount_whenOk() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        target.activateAccount("nikkinicholas.romero@gmail.com", "1234");

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient).activateAccount(eq("someId"), argumentCaptor.capture());
        AccountActivationRequest accountActivationRequest = argumentCaptor.getValue();
        assertThat(accountActivationRequest).isNotNull();
        assertThat(accountActivationRequest.isEmailVerified()).isTrue();
        assertThat(accountActivationRequest.isEnabled()).isTrue();
    }

    @Test
    public void activateAccount_whenUsernameDoesNotExist() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
            target.activateAccount("non-existent@gmail.com", "1234")
        );
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("non-existent@gmail.com");
        verify(keycloakUserClient, never()).activateAccount(any(), any());
    }

    @Test
    public void activateAccount_whenMultipleUsersMatchTheUsername() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("nikkinicholas.romero@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        KeycloakException e = assertThrows(KeycloakException.class, () ->
            target.activateAccount("nikkinicholas.romero@gmail.com", "1234")
        );
        assertThat(e.getMessage()).isEqualTo(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).activateAccount(any(), any());
    }

    @Test
    public void activateAccount_whenUserIsAlreadyActivated() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        keycloakUser1.setEmailVerified(true);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
            target.activateAccount("nikkinicholas.romero@gmail.com", "1234")
        );
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_ALREADY_ACTIVATED_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).activateAccount(any(), any());
    }

    @Test
    public void activateAccount_whenActivationCodeIsIncorrect() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(ACTIVATION_CODE, Collections.singletonList("1234"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
            target.activateAccount("nikkinicholas.romero@gmail.com", "4321")
        );
        assertThat(e.getMessage()).isEqualTo(ACTIVATION_CODE_IS_INCORRECT_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).activateAccount(any(), any());
    }
}
