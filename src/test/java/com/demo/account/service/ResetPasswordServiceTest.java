package com.demo.account.service;

import com.demo.account.client.KeycloakUserClient;
import com.demo.account.exception.KeycloakException;
import com.demo.account.exception.RequestException;
import com.demo.account.model.KeycloakResetPasswordRequest;
import com.demo.account.model.KeycloakUser;
import com.demo.account.model.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.demo.ErrorMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ResetPasswordServiceTest {
    private static final String PASSWORD_RESET_CODE = "passwordResetCode";

    @InjectMocks
    private ResetPasswordService target;

    @Mock
    private KeycloakUserClient keycloakUserClient;

    private ResetPasswordRequest request;

    @Captor
    private ArgumentCaptor<KeycloakResetPasswordRequest> argumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(target, "passwordResetCode", PASSWORD_RESET_CODE);

        request = new ResetPasswordRequest();
        request.setUsername("nikkinicholas.romero@gmail.com");
        request.setPassword("Password123$");
        request.setPasswordResetCode(UUID.randomUUID().toString());
    }

    @Test
    public void resetPassword() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(PASSWORD_RESET_CODE, Collections.singletonList(request.getPasswordResetCode()));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        target.resetPassword(request);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient).updateKeycloakAccountCredentials(eq("someId"), argumentCaptor.capture());
        KeycloakResetPasswordRequest keycloakResetPasswordRequest = argumentCaptor.getValue();
        assertThat(keycloakResetPasswordRequest).isNotNull();
        assertThat(keycloakResetPasswordRequest.getAttributes()).isNotEmpty();
        assertThat(keycloakResetPasswordRequest.getAttributes().size()).isEqualTo(1);
        assertThat(keycloakResetPasswordRequest.getAttributes().containsKey(PASSWORD_RESET_CODE)).isTrue();
        assertThat(keycloakResetPasswordRequest.getAttributes().get(PASSWORD_RESET_CODE)).isEqualTo(new ArrayList<>());
        assertThat(keycloakResetPasswordRequest.getCredentials()).isNotEmpty();
        assertThat(keycloakResetPasswordRequest.getCredentials().size()).isEqualTo(1);
        assertThat(keycloakResetPasswordRequest.getCredentials().get(0)).isNotNull();
        assertThat(keycloakResetPasswordRequest.getCredentials().get(0).getType()).isEqualTo("password");
        assertThat(keycloakResetPasswordRequest.getCredentials().get(0).getValue()).isEqualTo("Password123$");
        assertThat(keycloakResetPasswordRequest.getCredentials().get(0).isTemporary()).isFalse();
    }

    @Test
    public void resetPassword_whenUsernameDoesNotExist() {
        request.setUsername("non-existent@gmail.com");

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
                target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo(USERNAME_DOES_NOT_EXIST_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("non-existent@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountCredentials(anyString(), any());
    }

    @Test
    public void resetPassword_whenMultipleUsersMatchTheUsername() {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("nikkinicholas.romero@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        KeycloakException e = assertThrows(KeycloakException.class, () ->
                target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo(NON_UNIQUE_USERNAME_FOUND_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountCredentials(anyString(), any());
    }

    @Test
    public void resetPassword_whenUserIsNotYetActivated() {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(false);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
                target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo(ACCOUNT_IS_NOT_YET_ACTIVATED_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountCredentials(anyString(), any());
    }

    @Test
    public void resetPassword_whenUserAttributeIsEmpty() {
        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
        keycloakUser1.setAttributes(new HashMap<>());
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
                target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo(PASSWORD_RESET_CODE_INVALID_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountCredentials(anyString(), any());
    }

    @Test
    public void resetPassword_whenUserAttributePasswordResetCodeListIsEmpty() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(PASSWORD_RESET_CODE, new ArrayList<>());

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
                target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo(PASSWORD_RESET_CODE_INVALID_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountCredentials(anyString(), any());
    }

    @Test
    public void resetPassword_whenUserAttributePasswordResetCodeValueIsDoesNotMatch() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(PASSWORD_RESET_CODE, Collections.singletonList("someOtherValue"));

        KeycloakUser keycloakUser1 = new KeycloakUser();
        keycloakUser1.setId("someId");
        keycloakUser1.setUsername("nikkinicholas.romero@gmail.com");
        keycloakUser1.setEmailVerified(true);
        keycloakUser1.setAttributes(attributes);
        KeycloakUser keycloakUser2 = new KeycloakUser();
        keycloakUser2.setUsername("sayin.leslieanne@gmail.com");
        List<KeycloakUser> keycloakUserList = new ArrayList<>();
        keycloakUserList.add(keycloakUser1);
        keycloakUserList.add(keycloakUser2);
        when(keycloakUserClient.getUserListByUsername(anyString())).thenReturn(keycloakUserList);

        RequestException e = assertThrows(RequestException.class, () ->
                target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo(PASSWORD_RESET_CODE_INVALID_ERROR_MESSAGE);

        verify(keycloakUserClient).getUserListByUsername("nikkinicholas.romero@gmail.com");
        verify(keycloakUserClient, never()).updateKeycloakAccountCredentials(anyString(), any());
    }
}
